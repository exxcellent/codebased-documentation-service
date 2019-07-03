package business.generator.impl.generators;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;

import business.converter.InfoObjectConverter;
import business.converter.XMLObjectConverter;
import business.generator.impl.connectors.ServiceConnector;
import business.model.Dependency;
import collectors.models.InfoObject;
import collectors.models.maven.CollectedMavenInfoObject;
import collectors.models.restapi.CollectedAPIInfoObject;
import data.file.FileReader;
import data.file.JaxbStringMarshaller;
import data.interfaces.DataOutput;
import data.interfaces.DataOutputToFile;
import data.model.xml.Component;
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

		List<CollectedMavenInfoObject> infoObjects = getCollectedInfoObjects(DocumentationMojo.MAVEN_AGGREGATE_NAME + DocumentationMojo.SUFFIX, CollectedMavenInfoObject.class, srcFolders);
		List<CollectedAPIInfoObject> apiInfoObjects = getCollectedInfoObjects(DocumentationMojo.API_AGGREGATE_NAME + DocumentationMojo.SUFFIX, CollectedAPIInfoObject.class, srcFolders);

		List<Dependency> serviceDependencies = null;
		if (apiInfoObjects != null && !apiInfoObjects.isEmpty()) {
			ServiceConnector connector = new ServiceConnector();
			serviceDependencies = connector.connectServices(apiInfoObjects);
		}

		Map<String, String> cDD = generateComponentDependencyDescription(infoObjects, serviceDependencies);
		for (Entry<String, String> entry : cDD.entrySet()) {
			diagramFiles.addAll(output.writeToFile(entry.getValue(), entry.getKey().split("\\.")[0],
					entry.getKey().split("\\.")[1], targetFolder));
		}

		Map<String, String> mDD = generateModuleDependencyDescription(infoObjects);
		for (Entry<String, String> entry : mDD.entrySet()) {
			diagramFiles.addAll(output.writeToFile(entry.getValue(), entry.getKey().split("\\.")[0],
					entry.getKey().split("\\.")[1], targetFolder));
		}

		Map<String, String> sysD = generateSystemDescription(infoObjects, serviceDependencies);
		for (Entry<String, String> entry : sysD.entrySet()) {
			diagramFiles.addAll(output.writeToFile(entry.getValue(), entry.getKey().split("\\.")[0],
					entry.getKey().split("\\.")[1], targetFolder));
		}

		Map<String, String> servD = generateServiceDependencyDescription(infoObjects, serviceDependencies);
		for (Entry<String, String> entry : servD.entrySet()) {
			diagramFiles.addAll(output.writeToFile(entry.getValue(), entry.getKey().split("\\.")[0],
					entry.getKey().split("\\.")[1], targetFolder));
		}

		return diagramFiles;
	}

	public Map<String, String> generateDocuments(File... srcFolders) {
		Map<String, String> fileNameToContent = new HashMap<>();

		List<CollectedMavenInfoObject> infoObjects = getCollectedInfoObjects(DocumentationMojo.MAVEN_AGGREGATE_NAME + DocumentationMojo.SUFFIX, CollectedMavenInfoObject.class, srcFolders);
		List<CollectedAPIInfoObject> apiInfoObjects = getCollectedInfoObjects(DocumentationMojo.API_AGGREGATE_NAME + DocumentationMojo.SUFFIX, CollectedAPIInfoObject.class, srcFolders);

		List<Dependency> serviceDependencies = null;
		if (apiInfoObjects != null && !apiInfoObjects.isEmpty()) {
			ServiceConnector connector = new ServiceConnector();
			serviceDependencies = connector.connectServices(apiInfoObjects);
		}

		fileNameToContent.putAll(generateModuleDependencyDescription(infoObjects));
		fileNameToContent.putAll(generateComponentDependencyDescription(infoObjects, serviceDependencies));
		fileNameToContent.putAll(generateSystemDescription(infoObjects, serviceDependencies));
		fileNameToContent.putAll(generateServiceDependencyDescription(infoObjects, serviceDependencies));

		return fileNameToContent;
	}

	private <T extends InfoObject> List<T> getCollectedInfoObjects(String name, Class<T> clazz, File... srcFolders) {
		List<File> foundFiles = new ArrayList<>();
		for (File file : srcFolders) {
			foundFiles.addAll(FileReader.findFilesWithName(file, name, ".json"));
		}
		return InfoObjectConverter.createJSONObjects(foundFiles, clazz);
	}

	public Map<String, String> generateModuleDependencyDescription(List<CollectedMavenInfoObject> infoObjects) {

		Map<String, String> fileNameToContent = new HashMap<>();

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
			fileNameToContent.put("all_modules.xml", sysString);
		} catch (Exception e) {
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
				fileNameToContent.put(name + ".xml", serviceString);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return fileNameToContent;
	}

	public Map<String, String> generateComponentDependencyDescription(List<CollectedMavenInfoObject> infoObjects, List<Dependency> serviceDependencies) {
		Map<String, String> fileNameToContent = new HashMap<>();

		XMLObjectConverter creator = new XMLObjectConverter();
		List<data.model.xml.System> systems = creator.getSystems(infoObjects);
		List<Subsystem> subsys = creator.addSubsystems(infoObjects, systems);
		List<Service> services = creator.addServices(infoObjects, subsys);
		List<Module> modules = creator.addModules(infoObjects, services, false);
		List<Component> components = creator.addComponents(infoObjects, modules, true);

		
		ObjectFactory factory = new ObjectFactory();
		try {
			JaxbStringMarshaller marshaller = new JaxbStringMarshaller(data.model.xml.Service.class);
			marshaller.setMarshallerProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			for (Service service : services) {
				JAXBElement<Service> serviceElement = factory.createService(service);
				String serviceString = marshaller.marshall(serviceElement);
				String name = serviceNameToFileName(service, "components");
				fileNameToContent.put(name + ".xml", serviceString);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Systems sys = new Systems();
		sys.getSystem().addAll(systems);
		creator.addExternalComponentDependencies(components, serviceDependencies);		
		try {
			JaxbStringMarshaller marshaller = new JaxbStringMarshaller(data.model.xml.Systems.class);
			marshaller.setMarshallerProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			String sysString = marshaller.marshall(sys);
			fileNameToContent.put("all_components.xml", sysString);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return fileNameToContent;
	}

	public Map<String, String> generateSystemDescription(List<CollectedMavenInfoObject> infoObjects,
			List<Dependency> serviceDependencies) {
		Map<String, String> fileNameToContent = new HashMap<>();

		XMLObjectConverter creator = new XMLObjectConverter();
		List<data.model.xml.System> systems = creator.getSystems(infoObjects);
//		List<Subsystem> subsys = 
		creator.addSubsystems(infoObjects, systems);
//		creator.addServices(infoObjects, subsys);
		if (serviceDependencies != null) {
			creator.addSystemDependencies(infoObjects, serviceDependencies, systems);
		}

		Systems sys = new Systems();
		sys.getSystem().addAll(systems);

		try {
			JaxbStringMarshaller marshaller = new JaxbStringMarshaller(data.model.xml.Systems.class);
			marshaller.setMarshallerProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			String sysString = marshaller.marshall(sys);
			fileNameToContent.put("systems.xml", sysString);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return fileNameToContent;
	}

	public Map<String, String> generateServiceDependencyDescription(List<CollectedMavenInfoObject> infoObjects,
			List<Dependency> serviceDependencies) {
		Map<String, String> fileNameToContent = new HashMap<>();

		XMLObjectConverter creator = new XMLObjectConverter();
		List<data.model.xml.System> systems = creator.getSystems(infoObjects);
		List<Subsystem> subsys = creator.addSubsystems(infoObjects, systems);
		List<Service> services = creator.addServices(infoObjects, subsys);
		if (serviceDependencies != null) {
			creator.addServiceDependencies(infoObjects, serviceDependencies, services);
		}

		Systems sys = new Systems();
		sys.getSystem().addAll(systems);

		try {
			JaxbStringMarshaller marshaller = new JaxbStringMarshaller(data.model.xml.Systems.class);
			marshaller.setMarshallerProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			String sysString = marshaller.marshall(sys);
			fileNameToContent.put("services.xml", sysString);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return fileNameToContent;
	}

	private String serviceNameToFileName(Service service, String suffix) {
		return service.getName().trim().replaceAll("[:.,\\s]", "-").replaceAll("[-]+", "-") + "_" + suffix;
	}

}
