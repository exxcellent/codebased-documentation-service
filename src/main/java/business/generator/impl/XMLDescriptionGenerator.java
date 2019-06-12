package business.generator.impl;

import java.awt.ComponentOrientation;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContextFactory;
import org.jdom2.Document;
import org.jdom2.Element;

import business.converter.InfoObjectConverter;
import business.generator.interfaces.DocumentGenerator;
import collectors.models.maven.CollectedMavenInfoObject;
import collectors.models.maven.ComponentInfoObject;
import collectors.models.maven.ModuleInfoObject;
import collectors.models.maven.PackageInfoObject;
import data.file.FileReader;
import data.file.XMLFileWriter;
import mojos.DocumentationMojo;

/**
 * TODO: refactor!
 * 
 * @author gmittmann
 *
 */
public class XMLDescriptionGenerator implements DocumentGenerator {

//	private final String XSD_FILES_PATH = "resources/xml_descriptions";// this.getClass().getResource("/xml_descriptions/");

	@Override
	public List<File> generateDocuments(File targetFolder, File... srcFolders) {
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

		diagramFiles.addAll(generateComponentDependencyDescription(targetFolder, infoObjects));
		diagramFiles.addAll(generateModuleDependencyDescription(targetFolder, infoObjects));
		diagramFiles.addAll(generateSystemDescription(targetFolder, infoObjects));

		return diagramFiles;
	}

	public List<File> generateModuleDependencyDescription(File targetFolder,
			List<CollectedMavenInfoObject> infoObjects) {

		List<File> generatedFiles = new ArrayList<>();

		List<Element> microservices = new ArrayList<>();
		for (CollectedMavenInfoObject cMInfoObject : infoObjects) {
			Element root = new Element("modules");
			Document doc = new Document();

			Element microservice = new Element("microservice");
			Element micName = new Element("microservice-name");
			micName.addContent(cMInfoObject.getName());
			microservice.addContent(micName);

			List<Element> moduleList = new ArrayList<>();
			for (ModuleInfoObject moduleInfo : cMInfoObject.getModules()) {
				Element module = new Element("module");

				Element moduleName = new Element("module-name");
				moduleName.addContent(moduleInfo.getName());
				module.addContent(moduleName);

				Element moduleTag = new Element("module-id");
				moduleTag.addContent(moduleInfo.getTag());
				module.addContent(moduleTag);

				Element dependencies = new Element("dependencies");
				for (String moduleDep : moduleInfo.getDependsOn()) {
					Element dep = new Element("dependency");
					dep.addContent(moduleDep);
					dependencies.addContent(dep);
				}
				module.addContent(dependencies);
				moduleList.add(module);
			}

			microservice.addContent(moduleList);
			root.addContent(microservice);
			doc.setRootElement(root);

			File out = XMLFileWriter.writeToFile(doc, cMInfoObject.getTag() + "_module", targetFolder);
			if (out != null) {
				generatedFiles.add(out);
			}
			microservices.add(microservice.clone());
		}
		Element allRoot = new Element("microservices");
		allRoot.addContent(microservices);
		Document allDocument = new Document();
		allDocument.setRootElement(allRoot);

		File allOut = XMLFileWriter.writeToFile(allDocument, "all_module", targetFolder);
		if (allOut != null) {
			generatedFiles.add(allOut);
		}

		return generatedFiles;
	}

	public List<File> generateComponentDependencyDescription(File targetFolder,
			List<CollectedMavenInfoObject> infoObjects) {
		List<File> generatedFiles = new ArrayList<>();

		List<Element> microservices = new ArrayList<>();
		for (CollectedMavenInfoObject cMInfoObject : infoObjects) {
			Element root = new Element("components");
			Document doc = new Document();

			System.out.println("For: " + cMInfoObject.getName());
			Element microservice = new Element("microservice");
			Element micName = new Element("microservice-name");
			micName.addContent(cMInfoObject.getName());
			microservice.addContent(micName);

			Map<String, Element> moduleMap = new HashMap<>();
			for (ModuleInfoObject moduleInfo : cMInfoObject.getModules()) {
				Element module = new Element("module");
				Element moduleName = new Element("module-name");
				moduleName.addContent(moduleInfo.getName());
				module.addContent(moduleName);
				moduleMap.put(moduleInfo.getName(), module);
			}

			for (ComponentInfoObject components : cMInfoObject.getComponents()) {
				Element currentModule = moduleMap.get(components.getName());
				List<Element> componentList = new ArrayList<>();
				for (PackageInfoObject pkgInfo : components.getComponents()) {
					Element component = new Element("component");
					Element componentName = new Element("component-name");
					componentName.addContent(pkgInfo.getName());

					Element dependencies = new Element("dependencies");
					for (String dep : pkgInfo.getDependsOn()) {
						Element dependency = new Element("dependency");
						dependency.addContent(dep);
						dependencies.addContent(dependency);
					}
					component.addContent(componentName);
					component.addContent(dependencies);

					componentList.add(component);
				}
				currentModule.addContent(componentList);
			}

			microservice.addContent(moduleMap.values());
			root.addContent(microservice);
			doc.setRootElement(root);

			File out = XMLFileWriter.writeToFile(doc, cMInfoObject.getTag() + "_component", targetFolder);
			if (out != null) {
				generatedFiles.add(out);
			}
			microservices.add(microservice.clone());
		}
		Element allRoot = new Element("microservices");
		allRoot.addContent(microservices);
		Document allDocument = new Document();
		allDocument.setRootElement(allRoot);

		File allOut = XMLFileWriter.writeToFile(allDocument, "all_component", targetFolder);
		if (allOut != null) {
			generatedFiles.add(allOut);
		}

		return generatedFiles;
	}

	public List<File> generateSystemDescription(File targetFolder, List<CollectedMavenInfoObject> infoObjects) {
		List<File> generatedFiles = new ArrayList<>();
		
		Element root = new Element("systems");
		
		Map<String, Element> systems = new HashMap<>();
		for (CollectedMavenInfoObject info : infoObjects) {
			Element system = new Element("system");
			Element systemName = new Element("system-name");
			
			systemName.addContent(info.getSystem());
			system.addContent(systemName);
			
			systems.put(info.getSystem(), system);
		}

		for (CollectedMavenInfoObject info : infoObjects) {
			Element subsystem = new Element("subsystem");
			Element subsystemName = new Element("subsystem-name");
			
			subsystemName.addContent(info.getSubsystem());
			subsystem.addContent(subsystemName);
			
			systems.get(info.getSystem()).addContent(subsystem);			
		}
		
		root.addContent(systems.values());
		
		Document document = new Document();
		document.setRootElement(root);

		File out = XMLFileWriter.writeToFile(document, "systems", targetFolder);
		if (out != null) {
			generatedFiles.add(out);
		}
		
		return generatedFiles;
	}

//	public List<File> generateComponentDependencyDescription(File targetFolder, List<CollectedMavenInfoObject> infoObjects) {
//		List<File> generatedFiles = new ArrayList<>();
//		
//		String xsd = "component_diagram.xsd";
//		
//		try (InputStream xsdInputStream = this.getClass().getResourceAsStream(XSD_FILES_PATH + xsd)) {
//			System.out.println("START");
//			DynamicJAXBContext jaxbContext = DynamicJAXBContextFactory.createContextFromXSD(xsdInputStream, null, null, null);
//			System.out.println("created context");
////			System.setProperty("com.sun.tools.xjc.api.impl.s2j.SchemaCompilerImpl.noCorrectnessCheck", "true");
//			System.out.println("set property");
//			DynamicEntity componentEntity = jaxbContext.newDynamicEntity("example.Component");
//			System.out.println("created entity");
//			
//			for (CollectedMavenInfoObject info : infoObjects) {
//				for (ComponentInfoObject components : info.getComponents()) {
//					for (PackageInfoObject pack : components.getComponents()) {
//						componentEntity.set("component-name", pack.getName());
//						System.out.println("set component name");
////						componentEntity.set("dependant-name", pack.getDependsOn());
////						System.out.println("set dependant names");
//					}
//				}
//			}
//			
//			Marshaller marshaller = jaxbContext.createMarshaller();
//			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//			marshaller.marshal(componentEntity, System.out);
//			
//		} catch (IOException e) {
//			System.out.println("ERROR opening file");
//			System.out.println(e.getMessage());
//			
//		} catch (JAXBException e) {
//			System.out.println("ERROR marshalling & unmarshalling");
//			System.out.println(e.getMessage());
//		}
//		
//		
//		return generatedFiles;
//	}

}
