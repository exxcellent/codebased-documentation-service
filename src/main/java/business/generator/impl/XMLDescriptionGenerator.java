package business.generator.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.jdom2.Document;
import org.jdom2.Element;

import business.converter.InfoObjectConverter;
import business.generator.interfaces.DocumentGenerator;
import collectors.models.maven.CollectedMavenInfoObject;
import collectors.models.maven.ComponentInfoObject;
import collectors.models.maven.ModuleInfoObject;
import collectors.models.maven.PackageInfoObject;
import data.file.FileReader;
import data.file.JaxbStringMarshaller;
import data.file.XMLFileWriter;
import data.model.xml.Component;
import data.model.xml.Dependencies;
import data.model.xml.Module;
import data.model.xml.ObjectFactory;
import data.model.xml.Systems;
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
		
		XMLObjectCreator creator = new XMLObjectCreator();
		List<data.model.xml.System> systems = creator.getSystems(infoObjects);
		creator.addSubsystems(infoObjects, systems);
		Systems sys = new Systems();
		sys.getSystem().addAll(systems);
		
		try {
			JaxbStringMarshaller marshaller = new JaxbStringMarshaller(data.model.xml.Systems.class);
			String sysString = marshaller.marshall(sys);
			System.out.println("----");
			System.out.println(sysString);
			System.out.println("----");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return generatedFiles;
	}

	public List<File> generateComponentDependencyDescription(File targetFolder,
			List<CollectedMavenInfoObject> infoObjects) {
		List<File> generatedFiles = new ArrayList<>();
//		ObjectFactory factory = new ObjectFactory();
//		
//		for (CollectedMavenInfoObject info : infoObjects) {
//			Microservice microservice = factory.createMicroservice();
//			microservice.setMicroserviceName(info.getProjectName());
//			
//			
//			for (ModuleInfoObject moduleInfo : info.getModules()) {
//				Module mod = new Module();
//				mod.setModuleName(moduleInfo.getTag());
//				microservice.getModule().add(mod);
//			}
//			
//			for (ComponentInfoObject componentsInfo : info.getComponents()) {
//				Module mod = new Module();
//				mod.setModuleName(componentsInfo.getModuleName());
//				for (PackageInfoObject packInfo : componentsInfo.getComponents()) {
//					Component component = new Component();
//					component.setComponentName(packInfo.getPackageName());
//					
//					Dependencies dependencies = new Dependencies();
//					dependencies.getDependency().addAll(packInfo.getDependsOn());
//					
//				}
//				
//			}
//		}

		return generatedFiles;
	}

	public List<File> generateSystemDescription(File targetFolder, List<CollectedMavenInfoObject> infoObjects) {
		List<File> generatedFiles = new ArrayList<>();
		
		
		return generatedFiles;
	}

}
