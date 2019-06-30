package business.generator.impl.generators;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

import annotation.ConsumesAPI;
import business.converter.InfoObjectConverter;
import business.generator.impl.connectors.ServiceConnector;
import business.model.Dependency;
import business.model.SystemDescriptionModel;
import collectors.models.maven.CollectedMavenInfoObject;
import collectors.models.maven.ComponentInfoObject;
import collectors.models.maven.PackageInfoObject;
import collectors.models.restapi.CollectedAPIInfoObject;
import data.file.FileReader;
import data.interfaces.DataOutputToFile;
import mojos.DocumentationMojo;
import net.sourceforge.plantuml.Log;

/**
 * Creates diagrams based on available information in the source folders. The
 * diagrams are created via PlantUML and GraphViz. GraphViz has to be installed
 * on the system, or the created png and svg files are pictures of error
 * messages. The diagrams are saved into subfolders of the target folder: png
 * files in subfolder .\png, svg files in subfolder .\svg and the PlantUml
 * description in subfolder .\txt.
 * 
 * @author gmittmann
 *
 */
public class PlantUMLDiagramGenerator {
	
	private static String BEGIN_DIAGRAM = "@startuml\n skinparam componentStyle uml2\n\n";
	private static String  END_DIAGRAM = "@enduml\n";

	public List<File> generateDocuments(File targetFolder, boolean visualize, DataOutputToFile output,
			File... srcFolders) {
		List<File> diagramFiles = new ArrayList<>();

		// Find files and create CollectedMavenInfoObjects
		List<File> foundFiles = new ArrayList<>();
		for (File file : srcFolders) {
			foundFiles.addAll(FileReader.findFilesWithName(file,
					DocumentationMojo.MAVEN_AGGREGATE_NAME + DocumentationMojo.SUFFIX, ".json"));
			System.out.println("Files for Maven Info: ");
			for (File fl : foundFiles) {
				System.out.println(fl.getAbsolutePath());
			}
		}
		List<CollectedMavenInfoObject> infoObjects = InfoObjectConverter.createJSONObjects(foundFiles,
				CollectedMavenInfoObject.class);

		// Find files and create CollectedAPIInfoObjects
		List<File> foundAPIFiles = new ArrayList<>();
		for (File file : srcFolders) {
			foundAPIFiles.addAll(FileReader.findFilesWithName(file,
					DocumentationMojo.API_AGGREGATE_NAME + DocumentationMojo.SUFFIX, ".json"));
			System.out.println("Files for REST API Info: ");
			for (File fl : foundAPIFiles) {
				System.out.println(fl.getAbsolutePath());
			}
		}
		List<CollectedAPIInfoObject> apiInfoObjects = InfoObjectConverter.createJSONObjects(foundAPIFiles,
				CollectedAPIInfoObject.class);

		// TEST APIINFOOBJECTS
		System.out.println(apiInfoObjects.size());
		for (CollectedAPIInfoObject in : apiInfoObjects) {
			System.out.println(in.getServiceName() + " " + in.getConsume());
			System.out.println(in.getConsume() == null ? "NULL" : in.getConsume().size());
		}

		List<Dependency> serviceDependencies;
		if (apiInfoObjects != null && !apiInfoObjects.isEmpty()) {
			ServiceConnector connector = new ServiceConnector();
			serviceDependencies = connector.connectServices(apiInfoObjects);
			System.out.println("FOUND " + serviceDependencies.size() + " DEPENDENCIES");
		} else {
			serviceDependencies = null;
			System.out.println("FOUND NO DEPENDENCIES");
		}

		// Create diagrams
		diagramFiles.addAll(createModuleDiagram(infoObjects, targetFolder, visualize, output));
		diagramFiles.addAll(createComponentDiagram(infoObjects, targetFolder, visualize, output));
		diagramFiles.addAll(createSystemDiagram(infoObjects, targetFolder, visualize, output));
		diagramFiles.addAll(
				createSystemMicroserviceDiagram(infoObjects, serviceDependencies, targetFolder, visualize, output));

		return diagramFiles;
	}

	/**
	 * Creates package diagrams describing the dependencies between modules.
	 * 
	 * @param infoObjects  List of InfoObjects based on whom the diagrams are to be
	 *                     created.
	 * @param targetFolder Folder in which the subfolders and diagram files are
	 *                     written.
	 * @param visualize    if true, creates png and svg of the diagram, else just
	 *                     the textual description is created.
	 * @return List of created files.
	 */
	public List<File> createModuleDiagram(List<CollectedMavenInfoObject> infoObjects, File targetFolder,
			boolean visualize, DataOutputToFile output) {
		System.out.println("---- creating diagrams for modules ----");

		Map<String, String> umlDescriptions = new HashMap<>();
		for (CollectedMavenInfoObject currentInfo : infoObjects) {

			if (currentInfo.getModuleDependencies() != null) {
				String src = BEGIN_DIAGRAM;

				src += createModuleDiagramString(currentInfo);

				src += END_DIAGRAM;
				umlDescriptions.put(currentInfo.getTag(), src);
			} else {
				System.out.println("No info about module dependencies found for: " + currentInfo.getProjectName());
			}
		}

		List<File> diagramFiles = new ArrayList<>();
		for (Entry<String, String> descriptionEntry : umlDescriptions.entrySet()) {

			if (visualize) {
				diagramFiles.addAll(output.writeToFile(descriptionEntry.getValue(),
						generateDiagramName(descriptionEntry, "module"), ".png", targetFolder));
				diagramFiles.addAll(output.writeToFile(descriptionEntry.getValue(),
						generateDiagramName(descriptionEntry, "module"), ".svg", targetFolder));
			}
			diagramFiles.addAll(output.writeToFile(descriptionEntry.getValue(),
					generateDiagramNameForDescription(descriptionEntry, "module"), ".txt", targetFolder));

		}
		
		String allModules = createAllInServicesDiagramString(umlDescriptions, infoObjects);
		if (visualize) {
			diagramFiles.addAll(output.writeToFile(allModules, "All_modules_Diagram", ".png", targetFolder));
			diagramFiles.addAll(output.writeToFile(allModules, "All_modules_Diagram", ".svg", targetFolder));
		}
		diagramFiles.addAll(output.writeToFile(allModules, "All_modules_PlantUML_Description", ".txt", targetFolder));

		return diagramFiles;
	}

	/**
	 * Creates the String containing the actual PlantUML description of the diagram
	 * for the module dependencies.
	 * 
	 * @param infoObject CollectedMavenInfoObject containing the information about
	 *                   the modules.
	 * @return String containing the diagram description
	 */
	private String createModuleDiagramString(CollectedMavenInfoObject infoObject) {

		String diagramString = "";
		Map<String, List<String>> moduleDependencies = infoObject.getModuleDependencies();

		for (String module : moduleDependencies.keySet()) {
			diagramString += "package \"" + module + "\" {}\n";
		}
		diagramString += "\n";

		for (Entry<String, List<String>> entry : moduleDependencies.entrySet()) {
			for (String dep : entry.getValue()) {
				diagramString += "\"" + entry.getKey() + "\"" + " --> " + "\"" + dep + "\"" + "\n";
			}
		}

		return diagramString;
	}

	/**
	 * Creates a package diagram describing the dependencies between components in
	 * their packages.
	 * 
	 * @param infoObjects  List of InfoObjects based on whom the diagrams are to be
	 *                     created.
	 * @param targetFolder Folder in which the subfolders and diagram files are
	 *                     written.
	 * @param visualize    if true, creates png and svg of the diagram, else just
	 *                     the textual description is created.
	 * @return List of created files.
	 */
	public List<File> createComponentDiagram(List<CollectedMavenInfoObject> infoObjects, File targetFolder,
			boolean visualize, DataOutputToFile output) {
		System.out.println("---- creating diagrams for components ----");

		Map<String, String> umlDescriptions = new HashMap<>();
		for (CollectedMavenInfoObject currentInfo : infoObjects) {

			if (currentInfo.getModuleDependencies() != null) {
				String src = BEGIN_DIAGRAM;

				src += createComponentDiagramString(currentInfo);

				src += END_DIAGRAM;
				umlDescriptions.put(currentInfo.getTag(), src);
			} else {
				System.out.println("No info about module dependencies found for: " + currentInfo.getProjectName());
			}
		}
		
		String allDescriptions = createAllInServicesDiagramString(umlDescriptions, infoObjects);

		List<File> diagramFiles = new ArrayList<>();
		for (Entry<String, String> descriptionEntry : umlDescriptions.entrySet()) {

			if (visualize) {
				diagramFiles.addAll(output.writeToFile(descriptionEntry.getValue(),
						generateDiagramName(descriptionEntry, "component"), ".png", targetFolder));
				diagramFiles.addAll(output.writeToFile(descriptionEntry.getValue(),
						generateDiagramName(descriptionEntry, "component"), ".svg", targetFolder));
			}
			diagramFiles.addAll(output.writeToFile(descriptionEntry.getValue(),
					generateDiagramName(descriptionEntry, "component"), ".txt", targetFolder));

		}
		
		if (visualize) {
			diagramFiles.addAll(output.writeToFile(allDescriptions, "All_components_Diagram", ".png", targetFolder));
			diagramFiles.addAll(output.writeToFile(allDescriptions, "All_components_Diagram", ".svg", targetFolder));
		}
		diagramFiles.addAll(output.writeToFile(allDescriptions, "All_components_PlantUML_Description", ".txt", targetFolder));

		return diagramFiles;
	}

	private String createAllInServicesDiagramString(Map<String, String> umlDescriptions, List<CollectedMavenInfoObject> infoObjects) {
		String description = BEGIN_DIAGRAM;
		
		for (Entry<String, String> currentEntry : umlDescriptions.entrySet()) {
			String innerPart = removePlantUMLPart(currentEntry.getValue());
			
			description += "package \"" + tagToServiceName(currentEntry.getKey(), infoObjects) + "\" { \n";
			description += innerPart;
			description += "}\n\n";
			
		}
		description += END_DIAGRAM;
		
		return description;
	}
	

	private String removePlantUMLPart(String description) {
		return description.replace(BEGIN_DIAGRAM, "").replace(END_DIAGRAM, "");
	}

	/**
	 * Creates package diagrams describing the dependencies between components.
	 * 
	 * @param infoObjects  List of InfoObjects based on whom the diagrams are to be
	 *                     created.
	 * @param targetFolder Folder in which the subfolders and diagram files are
	 *                     written.
	 * @param visualize    if true, creates png and svg of the diagram, else just
	 *                     the textual description is created.
	 * @return List of created files.
	 */
	private String createComponentDiagramString(CollectedMavenInfoObject infoObject) {
		String diagramString = "";

		List<ComponentInfoObject> componentList = infoObject.getComponents();

		/* create packages & components */
		for (ComponentInfoObject moduleComponent : componentList) {
//			Map<String, List<String>> packageHierarchy = sortInnerPackages(moduleComponent);

			diagramString += "package " + "\"" + moduleComponent.getModuleName() + "\" { \n";

			for (PackageInfoObject info : moduleComponent.getComponents()) {
//				if (!isBeingInherited(packageHierarchy, info.getPackageName())) {
				diagramString += "[" + "\"" + info.getPackageName() + "\"] \n";
//				}
			}

			diagramString += "}\n\n";
		}

		diagramString += "\n";

		/* create dependencies between components */
		for (ComponentInfoObject moduleComponent : componentList) {
//			Map<String, List<String>> packageHierarchy = sortInnerPackages(moduleComponent);
//			Map<String, PackageInfoObject> pkgMapping = mapPkgInfoToName(moduleComponent);
			for (PackageInfoObject info : moduleComponent.getComponents()) {
				for (String dependency : info.getDependsOn()) {
					diagramString += "[\"" + info.getPackageName() + "\"]" + " ..> " + "[\"" + dependency
							+ "\"] : use \n";
				}
//				if (packageHierarchy.containsKey(info.getPackageName())) {
//					for (String inheritFrom : packageHierarchy.get(info.getPackageName())) {
//						PackageInfoObject inheritPackage = pkgMapping.get(inheritFrom);
//
//						for (String dependency : inheritPackage.getDependsOn()) {
//							diagramString += "[\"" + info.getPackageName() + "\"]" + " ..> " + "[\"" + dependency
//									+ "\"] : use \n";
//						}
//					}
//				}
			}

			diagramString += "\n";
		}

		return diagramString;
	}

//	private Map<String, List<String>> sortInnerPackages(ComponentInfoObject infoObject) {
//		List<String> packages = new ArrayList<>();
//		for (PackageInfoObject pkgInfo : infoObject.getComponents()) {
//			packages.add(pkgInfo.getPackageName());
//		}
//		Map<String, List<String>> packageHierarchy = new HashMap<>();
//		for (PackageInfoObject pkgInfo : infoObject.getComponents()) {
//			for (String otherName : packages) {
//				String currentName = pkgInfo.getPackageName();
//				if (currentName.startsWith(otherName)) {
//					if (packageHierarchy.containsKey(currentName)) {
//						packageHierarchy.get(currentName).add(otherName);
//					} else {
//						packageHierarchy.put(currentName, Lists.newArrayList(otherName));
//					}
//				}
//			}
//		}
//		return packageHierarchy;
//	}
//	
//	private boolean isBeingInherited(Map<String, List<String>> pkgHierarchy, String name) {
//		
//		for (List<String> currentList : pkgHierarchy.values()) {
//			if (currentList.contains(name)) {
//				return true;
//			}
//		}
//		
//		return false;
//	}
//
//	private Map<String, PackageInfoObject> mapPkgInfoToName(ComponentInfoObject baseInfo) {
//		Map<String, PackageInfoObject> mapping = new HashMap<>();
//
//		for (PackageInfoObject pkgInfo : baseInfo.getComponents()) {
//			mapping.put(pkgInfo.getPackageName(), pkgInfo);
//		}
//
//		return mapping;
//	}

	public List<File> createSystemDiagram(List<CollectedMavenInfoObject> infoObjects, File targetFolder,
			boolean visualize, DataOutputToFile output) {
		System.out.println("---- creating system diagram ----");

		Map<String, List<String>> sysToSubSys = new HashMap<>();
		for (CollectedMavenInfoObject currentInfo : infoObjects) {
			List<String> subSys = new ArrayList<>();
			subSys.add(currentInfo.getSubsystem());
			List<String> res = sysToSubSys.putIfAbsent(currentInfo.getSystem(), subSys);

			if (res != null) {
				subSys.addAll(res);
				sysToSubSys.put(currentInfo.getSystem(), subSys);
			}
		}

		String src = BEGIN_DIAGRAM;
		for (Entry<String, List<String>> entry : sysToSubSys.entrySet()) {
			src += createSystemDiagramString(entry);
		}
		src += END_DIAGRAM;

		Map<String, String> map = new HashMap<>();
		map.put("All", src);

		List<File> diagramFiles = new ArrayList<>();
		for (Entry<String, String> descriptionEntry : map.entrySet()) {
			if (visualize) {
				diagramFiles.addAll(output.writeToFile(descriptionEntry.getValue(),
						generateDiagramName(descriptionEntry, "system"), ".png", targetFolder));
				diagramFiles.addAll(output.writeToFile(descriptionEntry.getValue(),
						generateDiagramName(descriptionEntry, "system"), ".svg", targetFolder));
			}
			diagramFiles.addAll(output.writeToFile(descriptionEntry.getValue(),
					generateDiagramNameForDescription(descriptionEntry, "system"), ".txt", targetFolder));

		}

		return diagramFiles;

	}

	private String createSystemDiagramString(Entry<String, List<String>> entry) {
		String diagramDescription = "package \"" + entry.getKey() + "\" {\n";

		for (String subPackage : entry.getValue()) {
			diagramDescription += "package \"" + subPackage + "\" {}\n";
		}

		diagramDescription += "}\n\n";

		return diagramDescription;
	}

	public List<File> createSystemMicroserviceDiagram(List<CollectedMavenInfoObject> infoObjects,
			List<Dependency> serviceDependencies, File targetFolder, boolean visualize, DataOutputToFile output) {
		System.out.println("---- creating diagrams for microservices in system ----");

		List<SystemDescriptionModel> systems = new ArrayList<>();
		for (CollectedMavenInfoObject currentInfo : infoObjects) {
			SystemDescriptionModel sys = new SystemDescriptionModel(currentInfo.getSystem());
			if (systems.contains(sys)) {
				for (SystemDescriptionModel sdm : systems) {
					if (sys.equals(sdm)) {
						sys = sdm;
						break;
					}
				}
			} else {
				systems.add(sys);
			}

			sys.addSubsystem(currentInfo.getSubsystem(), currentInfo.getProjectName());

		}

		String src = BEGIN_DIAGRAM;
		for (SystemDescriptionModel model : systems) {
			src += createSystemMicroserviceDiagramString(model);
		}

		if (serviceDependencies != null) {
			for (Dependency dependency : serviceDependencies) {
				if (dependency.getDependsOn().equalsIgnoreCase("external")
						|| dependency.getDependsOn().equalsIgnoreCase(ConsumesAPI.DEFAULT_SERVICE)) {
					src += "package \"external\" {}\n";
					break;
				}
			}
			src += createServiceDependencies(serviceDependencies);
		}

		src += END_DIAGRAM;

		Map<String, String> map = new HashMap<>();
		map.put("All", src);

		List<File> diagramFiles = new ArrayList<>();
		for (Entry<String, String> descriptionEntry : map.entrySet()) {
			if (visualize) {
				diagramFiles.addAll(output.writeToFile(descriptionEntry.getValue(),
						generateDiagramName(descriptionEntry, "microservice_system"), ".png", targetFolder));
				diagramFiles.addAll(output.writeToFile(descriptionEntry.getValue(),
						generateDiagramName(descriptionEntry, "microservice_system"), ".svg", targetFolder));
			}
			diagramFiles.addAll(output.writeToFile(descriptionEntry.getValue(),
					generateDiagramNameForDescription(descriptionEntry, "microservice_system"), ".txt", targetFolder));

		}

		return diagramFiles;

	}

	private String createSystemMicroserviceDiagramString(SystemDescriptionModel model) {
		String diagramDescription = "package \"" + model.getSystemName() + "\" {\n";
		for (Entry<String, List<String>> subPackage : model.getSubsysToMS().entrySet()) {
			diagramDescription += "package \"" + subPackage.getKey() + "\" {\n";
			for (String ms : subPackage.getValue()) {
				diagramDescription += "package \"" + ms + "\" {}\n";
			}

			diagramDescription += "}\n";
		}
		diagramDescription += "}\n\n";

		return diagramDescription;
	}

	private String createServiceDependencies(List<Dependency> serviceDependencies) {
		String diagramDescriptionServiceDependencies = "";

		if (serviceDependencies == null || serviceDependencies.isEmpty()) {
			Log.info("No dependencies between services found");
			return diagramDescriptionServiceDependencies;
		} 

		for (Dependency dependency : serviceDependencies) {
			diagramDescriptionServiceDependencies += "\"" + dependency.getService() + "\"" + "-->" + "\""
					+ dependency.getDependsOn() + "\" : \"" + dependency.getMethod() + " : " + dependency.getPath() + "\"\n";
		}

		return diagramDescriptionServiceDependencies;
	}

//	public List<File> createAllInfoComponentDiagram(SystemDescriptionModel systemdescription,
//			List<CollectedMavenInfoObject> infoObjects, File targetFolder, boolean visualize, DataOutputToFile output) {
//		List<File> files = new ArrayList<>();
//		
//		Map<String, String> umlDescriptions = new HashMap<>();
//		String src = "@startuml\n";
//		src += "skinparam componentStyle uml2\n\n";
//		List<String> services = new ArrayList<>();
//		for (CollectedMavenInfoObject currentInfo : infoObjects) {
//
//			if (currentInfo.getModuleDependencies() != null) {
//				src += "package \"" + currentInfo.getProjectName() + "\" {\n";;
//				
//				src += createComponentDiagramString(currentInfo);
//
//				umlDescriptions.put(currentInfo.getTag(), src);
//			} else {
//				System.out.println("No info about module dependencies found for: " + currentInfo.getProjectName());
//			}
//		}
//		src += "@enduml\n";
//
//		return files;
//	}

	private String generateDiagramName(Entry<String, String> descriptionEntry, String diagramType) {
		return descriptionEntry.getKey() + "_" + diagramType + "_Diagram";
	}

	private String generateDiagramNameForDescription(Entry<String, String> descriptionEntry, String diagramType) {
		return descriptionEntry.getKey() + "_" + diagramType + "_PlantUML_Description";
	}
	
	private String tagToServiceName (String tag, List<CollectedMavenInfoObject> infoObjects) {
		String name = tag;
		
		for (CollectedMavenInfoObject info : infoObjects) {
			if (info.getTag().equals(tag)) {
				return info.getProjectName();
			}
		}
		
		return name;		
	}
}