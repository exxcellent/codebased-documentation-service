package business.generator.impl.generators;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import business.converter.FileToInfoObjectConverter;
import business.converter.PlantUMLStringConverter;
import business.generator.impl.connectors.ServiceConnector;
import business.generator.impl.creator.SystemsFactory;
import business.generator.impl.creator.SystemsFactory.DependencyPlacement;
import business.model.Dependency;
import collectors.models.maven.CollectedMavenInfoObject;
import collectors.models.restapi.CollectedAPIInfoObject;
import data.file.FileWriter;
import data.file.JaxbStringMarshaller;
import data.interfaces.DataOutputToFile;
import data.model.xml.Systems;
import data.model.xml.ObjectFactory;
import data.model.xml.System;
import mojos.DocumentationMojo;

public class DocumentationGenerator {

	public void createDocumentation(DataOutputToFile output, boolean pltUml, boolean xml, boolean viz,
			File... srcFolders) {

		List<CollectedMavenInfoObject> infoObjects = FileToInfoObjectConverter.getCollectedInfoObjects(
				DocumentationMojo.MAVEN_AGGREGATE_NAME + DocumentationMojo.SUFFIX, CollectedMavenInfoObject.class,
				srcFolders);
		List<CollectedAPIInfoObject> apiInfoObjects = FileToInfoObjectConverter.getCollectedInfoObjects(
				DocumentationMojo.API_AGGREGATE_NAME + DocumentationMojo.SUFFIX, CollectedAPIInfoObject.class,
				srcFolders);

		List<Dependency> serviceDependencies = new ArrayList<>();
		if (apiInfoObjects != null && !apiInfoObjects.isEmpty()) {
			ServiceConnector connector = new ServiceConnector();
			serviceDependencies = connector.connectServices(apiInfoObjects, infoObjects);
		}

		SystemsFactory factory = new SystemsFactory(infoObjects, serviceDependencies);
		SystemsCustomizer customizer = new SystemsCustomizer();

		Set<DependencyPlacement> servicePlacements = new HashSet<>();
		servicePlacements.add(DependencyPlacement.SERVICE);

		Systems systemsServiceWRest = customizer.getServicesWithRestDiagram(factory.getBaseSystemsWithRest(servicePlacements));
		Systems systemsModules = customizer.getModuleDiagram(factory.getBaseSystems());
		Systems systemsComponents = customizer.getComponentModuleDiagram(factory.getBaseSystems());

		Set<DependencyPlacement> componentPlacements = new HashSet<>();
		componentPlacements.add(DependencyPlacement.COMPONENT);
		Systems systemsComponentsWRest = customizer
				.getComponentModuleDiagram(factory.getBaseSystemsWithRest(componentPlacements));

		if (pltUml) {
			java.lang.System.out.println(" - PLANTUML");
			PlantUMLStringConverter pltUmlConverter = new PlantUMLStringConverter();

			String allSystemsServiceWRestString = pltUmlConverter.convertSystems(systemsServiceWRest.getSystem());
			writePlantUMLForEachSystem(pltUmlConverter, systemsServiceWRest.getSystem(), output, viz, "_services");

			String allSystemsModulesString = pltUmlConverter.convertSystems(systemsModules.getSystem());
			writePlantUMLForEachSystem(pltUmlConverter, systemsModules.getSystem(), output, viz, "_modules");

			String allSystemsComponentsString = pltUmlConverter.convertSystems(systemsComponents.getSystem());
			writePlantUMLForEachSystem(pltUmlConverter, systemsComponents.getSystem(), output, viz, "_components");

			String allSystemsComponentsWRestString = pltUmlConverter.convertSystems(systemsComponentsWRest.getSystem());

			output.writeToFile(allSystemsServiceWRestString, "all_services", "txt");
			output.writeToFile(allSystemsModulesString, "all_modules", "txt");
			output.writeToFile(allSystemsComponentsString, "all_components", "txt");
			output.writeToFile(allSystemsComponentsWRestString, "all_components_rest", "txt");
			if (viz) {
				output.writeToFile(allSystemsServiceWRestString, "all_services", "svg");
				output.writeToFile(allSystemsModulesString, "all_modules", "svg");
				output.writeToFile(allSystemsComponentsString, "all_components", "svg");
				output.writeToFile(allSystemsComponentsWRestString, "all_components_rest", "svg");
			}
			java.lang.System.out.println(" - FINISHED PLANTUML");
			if (output instanceof FileWriter) {
				java.lang.System.out.println(" - FILES IN: ");
				java.lang.System.out.println("   " + ((FileWriter) output).getTargetFolderName() + "\\txt");
				if (viz) {
					java.lang.System.out
							.println("   " + ((FileWriter) output).getTargetFolderName() + "\\svg");
				}
			}
		}

		if (xml) {
			try {
				java.lang.System.out.println(" - XML");
				JaxbStringMarshaller systemMarshaller = new JaxbStringMarshaller(System.class);
				systemMarshaller.setMarshallerProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
				JaxbStringMarshaller systemsMarshaller = new JaxbStringMarshaller(Systems.class);
				systemsMarshaller.setMarshallerProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

				String systemsServiceWRestString = systemsMarshaller.marshall(systemsServiceWRest);
				writeXMLForEachSystem(systemsServiceWRest.getSystem(), systemMarshaller, output, "_services");
				output.writeToFile(systemsServiceWRestString, "all_services", "xml");

				String systemsModulesString = systemsMarshaller.marshall(systemsModules);
				writeXMLForEachSystem(systemsModules.getSystem(), systemMarshaller, output, "_modules");
				output.writeToFile(systemsModulesString, "all_modules", "xml");

				String systemsComponentsString = systemsMarshaller.marshall(systemsComponents);
				writeXMLForEachSystem(systemsComponents.getSystem(), systemMarshaller, output, "_components");
				output.writeToFile(systemsComponentsString, "all_components", "xml");

				String systemsComponentsWRestString = systemsMarshaller.marshall(systemsComponentsWRest);
				output.writeToFile(systemsComponentsWRestString, "all_components_rest", "xml");
				
				java.lang.System.out.println(" - FINISHED XML");
				if (output instanceof FileWriter) {
					java.lang.System.out.println(" - FILES IN: ");
					java.lang.System.out.println("   " + ((FileWriter) output).getTargetFolderName() + "\\xml");
				}

			} catch (Exception e) {
				java.lang.System.out.println("Marshaller did not work : " + e.getMessage());
				e.printStackTrace();
			}
		}

	}

	public void writePlantUMLForEachSystem(PlantUMLStringConverter pltUmlConverter, List<System> systemList,
			DataOutputToFile output, boolean visualize, String suffix) {
		for (System system : systemList) {
			String currentSysString = pltUmlConverter.convertSystems(system);
			output.writeToFile(currentSysString, system.getName() + suffix, "txt");
			if (visualize) {
				output.writeToFile(currentSysString, system.getName() + suffix, "svg");
			}
		}
	}

	public void writeXMLForEachSystem(List<System> systemList, JaxbStringMarshaller systemMarshaller,
			DataOutputToFile output, String suffix) throws JAXBException {
		ObjectFactory objectFactory = new ObjectFactory();
		for (System system : systemList) {
			JAXBElement<System> systemElement = objectFactory.createSystem(system);
			String systemString = systemMarshaller.marshall(systemElement);
			output.writeToFile(systemString, system.getName() + suffix, "xml");
		}
	}

}
