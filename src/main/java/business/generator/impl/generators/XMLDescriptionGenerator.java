package business.generator.impl.generators;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;

import business.converter.InfoObjectConverter;
import business.converter.XMLObjectConverter;
import business.generator.impl.connectors.ServiceConnector;
import business.model.Dependency;
import collectors.models.maven.CollectedMavenInfoObject;
import collectors.models.restapi.CollectedAPIInfoObject;
import data.file.FileReader;
import data.file.JaxbStringMarshaller;
import data.interfaces.DataOutputToFile;
import data.model.xml.Module;
import data.model.xml.ObjectFactory;
import data.model.xml.Service;
import data.model.xml.Subsystem;
import data.model.xml.Systems;
import mojos.DocumentationMojo;

/**
 * TODO: refactor!
 * 
 * @author gmittmann
 *
 */
public class XMLDescriptionGenerator {

//	private final String XSD_FILES_PATH = "resources/xml_descriptions";// this.getClass().getResource("/xml_descriptions/");

	public List<File> generateDocuments(File targetFolder, DataOutputToFile output, File... srcFolders) {
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

		// Find files and create CollectedAPIInfoObjects
		List<File> foundAPIFiles = new ArrayList<>();
		for (File file : srcFolders) {
			foundAPIFiles.addAll(FileReader.findFilesWithName(file,
					DocumentationMojo.API_AGGREGATE_NAME + DocumentationMojo.SUFFIX, ".json"));
			for (File fl : foundAPIFiles) {
				System.out.println(fl.getAbsolutePath());
			}
		}
		List<CollectedAPIInfoObject> apiInfoObjects = InfoObjectConverter.createJSONObjects(foundAPIFiles,
				CollectedAPIInfoObject.class);

		if (apiInfoObjects != null && !apiInfoObjects.isEmpty()) {
			ServiceConnector connector = new ServiceConnector();
			List<Dependency> serviceDependencies = connector.connectServices(apiInfoObjects);
		}

		diagramFiles.addAll(generateComponentDependencyDescription(targetFolder, infoObjects, output));
		diagramFiles.addAll(generateModuleDependencyDescription(targetFolder, infoObjects, output));
		diagramFiles.addAll(generateSystemDescription(targetFolder, infoObjects, output));

		return diagramFiles;
	}

	public List<File> generateModuleDependencyDescription(File targetFolder, List<CollectedMavenInfoObject> infoObjects,
			DataOutputToFile output) {

		List<File> generatedFiles = new ArrayList<>();

		XMLObjectConverter creator = new XMLObjectConverter();
		List<data.model.xml.System> systems = creator.getSystems(infoObjects);
		List<Subsystem> subsys = creator.addSubsystems(infoObjects, systems);
		List<Service> services = creator.addServices(infoObjects, subsys);
		creator.addModules(infoObjects, services, true);

		Systems sys = new Systems();
		sys.getSystem().addAll(systems);

		try {
			JaxbStringMarshaller marshaller = new JaxbStringMarshaller(data.model.xml.Systems.class);
			marshaller.setMarshallerProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			String sysString = marshaller.marshall(sys);
			generatedFiles.addAll(output.writeToFile(sysString, "all_modules", ".xml", targetFolder));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ObjectFactory factory = new ObjectFactory();
		try {
			JaxbStringMarshaller marshaller = new JaxbStringMarshaller(data.model.xml.Service.class);
			marshaller.setMarshallerProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			for (Service service : services) {
				JAXBElement<Service> serviceElement = factory.createService(service);
				String serviceString = marshaller.marshall(serviceElement);
				String name = serviceNameToFileName(service, "modules");
				generatedFiles.addAll(output.writeToFile(serviceString, name, ".xml", targetFolder));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return generatedFiles;
	}

	public List<File> generateComponentDependencyDescription(File targetFolder,
			List<CollectedMavenInfoObject> infoObjects, DataOutputToFile output) {
		List<File> generatedFiles = new ArrayList<>();

		XMLObjectConverter creator = new XMLObjectConverter();
		List<data.model.xml.System> systems = creator.getSystems(infoObjects);
		List<Subsystem> subsys = creator.addSubsystems(infoObjects, systems);
		List<Service> services = creator.addServices(infoObjects, subsys);
		List<Module> modules = creator.addModules(infoObjects, services, false);
		creator.addComponents(infoObjects, modules, true);

		Systems sys = new Systems();
		sys.getSystem().addAll(systems);
		ObjectFactory factory = new ObjectFactory();

		try {
			JaxbStringMarshaller marshaller = new JaxbStringMarshaller(data.model.xml.Systems.class);
			marshaller.setMarshallerProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			String sysString = marshaller.marshall(sys);
			generatedFiles.addAll(output.writeToFile(sysString, "all_components", ".xml", targetFolder));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			JaxbStringMarshaller marshaller = new JaxbStringMarshaller(data.model.xml.Service.class);
			marshaller.setMarshallerProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			for (Service service : services) {
				JAXBElement<Service> serviceElement = factory.createService(service);
				String serviceString = marshaller.marshall(serviceElement);
				String name = serviceNameToFileName(service, "components");
				generatedFiles.addAll(output.writeToFile(serviceString, name, ".xml", targetFolder));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return generatedFiles;
	}

	public List<File> generateSystemDescription(File targetFolder, List<CollectedMavenInfoObject> infoObjects,
			DataOutputToFile output) {
		List<File> generatedFiles = new ArrayList<>();

		XMLObjectConverter creator = new XMLObjectConverter();
		List<data.model.xml.System> systems = creator.getSystems(infoObjects);
		List<Subsystem> subsys = creator.addSubsystems(infoObjects, systems);
		List<Service> services = creator.addServices(infoObjects, subsys);
		creator.addServiceDependencies(infoObjects, services);

		Systems sys = new Systems();
		sys.getSystem().addAll(systems);

		try {
			JaxbStringMarshaller marshaller = new JaxbStringMarshaller(data.model.xml.Systems.class);
			marshaller.setMarshallerProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			String sysString = marshaller.marshall(sys);
			generatedFiles.addAll(output.writeToFile(sysString, "services", ".xml", targetFolder));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return generatedFiles;
	}

	public List<File> generateServiceDependencyDescription(File targetFolder,
			List<CollectedMavenInfoObject> infoObjects, List<Dependency> serviceDependencies, DataOutputToFile output) {
		List<File> generatedFiles = new ArrayList<>();

		return generatedFiles;
	}

	private String serviceNameToFileName(Service service, String suffix) {
		return service.getName().trim().replaceAll("[:.,\\s]", "-").replaceAll("[-]+", "-") + "_" + suffix;
	}

}
