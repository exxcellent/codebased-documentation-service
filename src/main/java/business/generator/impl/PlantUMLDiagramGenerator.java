package business.generator.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import business.converter.InfoObjectConverter;
import business.model.SystemDescriptionModel;
import collectors.models.maven.CollectedMavenInfoObject;
import collectors.models.maven.ComponentInfoObject;
import collectors.models.maven.PackageInfoObject;
import data.file.FileReader;
import data.interfaces.DataOutputToFile;
import mojos.DocumentationMojo;

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
	
	public List<File> generateDocuments(File targetFolder, boolean visualize, DataOutputToFile output, File... srcFolders) {
		List<File> diagramFiles = new ArrayList<>();

		// Find files and create CollectedMavenInfoObjects
		List<File> foundFiles = new ArrayList<>();
		for (File file : srcFolders) {
			foundFiles.addAll(FileReader.findFilesWithName(file,
					DocumentationMojo.MAVEN_AGGREGATE_NAME + DocumentationMojo.SUFFIX, ".json"));
			for (File fl : foundFiles) {
				System.out.println(fl.getAbsolutePath());
			}
		}
		List<CollectedMavenInfoObject> infoObjects = InfoObjectConverter.createJSONObjects(foundFiles,
				CollectedMavenInfoObject.class);

		// Create diagrams
		diagramFiles.addAll(createModuleDiagram(infoObjects, targetFolder, visualize, output));
		diagramFiles.addAll(createComponentDiagram(infoObjects, targetFolder, visualize, output));
		diagramFiles.addAll(createSystemDiagram(infoObjects, targetFolder, visualize, output));
		diagramFiles.addAll(createSystemMicroserviceDiagram(infoObjects, targetFolder, visualize, output));

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
				String src = "@startuml\n";
				src += "skinparam componentStyle uml2\n\n";

				src += createModuleDiagramString(currentInfo);

				src += "@enduml\n";
				umlDescriptions.put(currentInfo.getTag(), src);
			} else {
				System.out.println("No info about module dependencies found for: " + currentInfo.getProjectName());
			}
		}

		List<File> diagramFiles = new ArrayList<>();
		for (Entry<String, String> descriptionEntry : umlDescriptions.entrySet()) {

			if (visualize) {
				diagramFiles.addAll(output.writeToFile(descriptionEntry.getValue(), generateDiagramName(descriptionEntry, "module"), ".png", targetFolder));
				diagramFiles.addAll(output.writeToFile(descriptionEntry.getValue(), generateDiagramName(descriptionEntry, "module"), ".svg", targetFolder));
			}
			diagramFiles.addAll(output.writeToFile(descriptionEntry.getValue(), generateDiagramNameForDescription(descriptionEntry, "module"), ".txt", targetFolder));

		}

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
				String src = "@startuml\n";
				src += "skinparam componentStyle uml2\n\n";

				src += createComponentDiagramString(currentInfo);

				src += "@enduml\n";
				umlDescriptions.put(currentInfo.getTag(), src);
			} else {
				System.out.println("No info about module dependencies found for: " + currentInfo.getProjectName());
			}
		}

		List<File> diagramFiles = new ArrayList<>();
		for (Entry<String, String> descriptionEntry : umlDescriptions.entrySet()) {

			if (visualize) {
				diagramFiles.addAll(output.writeToFile(descriptionEntry.getValue(), generateDiagramName(descriptionEntry, "component"), ".png", targetFolder));
				diagramFiles.addAll(output.writeToFile(descriptionEntry.getValue(), generateDiagramName(descriptionEntry, "component"), ".svg", targetFolder));
			}
			diagramFiles.addAll(output.writeToFile(descriptionEntry.getValue(), generateDiagramName(descriptionEntry, "component"), ".txt", targetFolder));

		}

		return diagramFiles;
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

		/* create packages */
		for (ComponentInfoObject moduleComponent : componentList) {
			diagramString += "package " + "\"" + moduleComponent.getModuleName() + "\" { \n";

			for (PackageInfoObject info : moduleComponent.getComponents()) {
				diagramString += "package " + "\"" + info.getPackageName() + "\" {} \n";
			}

			diagramString += "}\n\n";
		}

		diagramString += "\n";

		/* create dependencies between packages */
		for (ComponentInfoObject moduleComponent : componentList) {
			for (PackageInfoObject info : moduleComponent.getComponents()) {
				for (String dependency : info.getDependsOn()) {
					diagramString += "\"" + info.getPackageName() + "\"" + " --> " + "\"" + dependency + "\"\n";
				}
			}

			diagramString += "\n";
		}

		return diagramString;
	}

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

		String src = "@startuml\n";
		src += "skinparam componentStyle uml2\n\n";
		for (Entry<String, List<String>> entry : sysToSubSys.entrySet()) {
			src += createSystemDiagramString(entry);
		}
		src += "@enduml\n";

		Map<String, String> map = new HashMap<>();
		map.put("All", src);

		List<File> diagramFiles = new ArrayList<>();
		for (Entry<String, String> descriptionEntry : map.entrySet()) {
			if (visualize) {
				diagramFiles.addAll(output.writeToFile(descriptionEntry.getValue(), generateDiagramName(descriptionEntry, "system"), ".png", targetFolder));
				diagramFiles.addAll(output.writeToFile(descriptionEntry.getValue(), generateDiagramName(descriptionEntry, "system"), ".svg", targetFolder));
			}
			diagramFiles.addAll(output.writeToFile(descriptionEntry.getValue(), generateDiagramNameForDescription(descriptionEntry, "system"), ".txt", targetFolder));

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

	public List<File> createSystemMicroserviceDiagram(List<CollectedMavenInfoObject> infoObjects, File targetFolder,
			boolean visualize, DataOutputToFile output) {
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

		String src = "@startuml\n";
		src += "skinparam componentStyle uml2\n";
		for (SystemDescriptionModel model : systems) {
			src += createSystemMicroserviceDiagramString(model);
		}
		src += "@enduml\n";

		Map<String, String> map = new HashMap<>();
		map.put("All", src);

		List<File> diagramFiles = new ArrayList<>();
		for (Entry<String, String> descriptionEntry : map.entrySet()) {
			if (visualize) {
				diagramFiles.addAll(output.writeToFile(descriptionEntry.getValue(), generateDiagramName(descriptionEntry, "microservice_system"), ".png", targetFolder));
				diagramFiles.addAll(output.writeToFile(descriptionEntry.getValue(), generateDiagramName(descriptionEntry, "microservice_system"), ".svg", targetFolder));
			}
			diagramFiles.addAll(output.writeToFile(descriptionEntry.getValue(), generateDiagramNameForDescription(descriptionEntry, "microservice_system"), ".txt", targetFolder));

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
	
	private String generateDiagramName(Entry<String, String> descriptionEntry, String diagramType) {
		return descriptionEntry.getKey() + "_" + diagramType + "_Diagram";
	}
	
	private String generateDiagramNameForDescription(Entry<String, String> descriptionEntry, String diagramType) {
		return descriptionEntry.getKey() + "_" + diagramType + "_PlantUML_Description";
	}
}
