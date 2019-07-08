package business.generator.impl.creator;

import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import business.converter.XMLObjectConverter;
import business.model.Dependency;
import collectors.models.maven.CollectedMavenInfoObject;
import data.file.JaxbStringMarshaller;
import data.file.JaxbStringUnmarshaller;
import data.model.xml.Component;
import data.model.xml.Module;
import data.model.xml.Service;
import data.model.xml.Subsystem;
import data.model.xml.Systems;

public class SystemsFactory {

	private List<CollectedMavenInfoObject> infoObjects;
	private List<Dependency> serviceDependencies;

	private String baseSystems;
	private String baseSystemsWithoutModules;

	public SystemsFactory(List<CollectedMavenInfoObject> infoObjects, List<Dependency> serviceDependencies) {
		this.infoObjects = infoObjects;
		this.serviceDependencies = serviceDependencies;
	}

	public Systems getBaseSystems() {
		if (baseSystems == null || baseSystems.isEmpty()) {
			Systems systems = createBaseSystems();
			this.baseSystems = createBaseSystemsString(systems);
			return systems;
		}
		return createSystemsFromXML(baseSystems);
	}
	
	public Systems getBaseSystemsWithRest(Set<DependencyPlacement> placementOfRestDependencies) {
			return createBaseSystemsRest(placementOfRestDependencies);
	}

	public Systems getBaseSystemsWithoutModules() {
		if (baseSystemsWithoutModules == null || baseSystemsWithoutModules.isEmpty()) {
			Systems systems = createBaseSystemsWithoutModule();
			this.baseSystemsWithoutModules = createBaseSystemsString(systems);
			return systems;
		}
		return createSystemsFromXML(baseSystemsWithoutModules);
	}

	private String createBaseSystemsString(Systems baseSystem) {
		try {
			final JaxbStringMarshaller jaxbMarshaller = new JaxbStringMarshaller(data.model.xml.Systems.class);
			return jaxbMarshaller.marshall(baseSystem);
		} catch (Exception e) {
			System.err.println("Error while marshalling systems:");
			System.err.println(e.getMessage());
			return "";
		}
	}

	private Systems createSystemsFromXML(String systemsString) {
		try {
			final JaxbStringUnmarshaller jaxbUnmarshaller = new JaxbStringUnmarshaller(data.model.xml.Systems.class);
			return jaxbUnmarshaller.unmarshall(systemsString);
		} catch (JAXBException e) {
			System.err.println("Error while marshalling systems:");
			System.err.println(e.getMessage());
			return null;
		}

	}

	private Systems createBaseSystems() {
		Systems systems = new Systems();
		XMLObjectConverter creator = new XMLObjectConverter();

		List<data.model.xml.System> system = creator.getSystems(infoObjects);
		creator.addSystemDependencies(infoObjects, serviceDependencies, system);

		List<Subsystem> subsys = creator.addSubsystems(infoObjects, system);
		creator.addSubystemDependencies(infoObjects, serviceDependencies, subsys);

		List<Service> services = creator.addServices(infoObjects, subsys);
		creator.addServiceDependencies(infoObjects, serviceDependencies, services);

		List<Module> modules = creator.addModules(infoObjects, services, true);
		creator.addComponents(infoObjects, modules, true);

		systems.getSystem().addAll(system);

		return systems;
	}

	private Systems createBaseSystemsWithoutModule() {
		Systems systems = new Systems();
		XMLObjectConverter creator = new XMLObjectConverter();

		List<data.model.xml.System> system = creator.getSystems(infoObjects);
		creator.addSystemDependencies(infoObjects, serviceDependencies, system);

		List<Subsystem> subsys = creator.addSubsystems(infoObjects, system);
		creator.addSubystemDependencies(infoObjects, serviceDependencies, subsys);

		List<Service> services = creator.addServices(infoObjects, subsys);
		creator.addServiceDependencies(infoObjects, serviceDependencies, services);

		creator.addComponentsToService(infoObjects, services, true);

		systems.getSystem().addAll(system);

		return systems;
	}

	private Systems createBaseSystemsRest(Set<DependencyPlacement> placeForRestDependency) {
		Systems systems = new Systems();
		XMLObjectConverter creator = new XMLObjectConverter();

		List<data.model.xml.System> system = creator.getSystems(infoObjects);
		List<Subsystem> subsys = creator.addSubsystems(infoObjects, system);
		List<Service> services = creator.addServices(infoObjects, subsys);
		List<Module> modules = creator.addModules(infoObjects, services, true);
		List<Component> components = creator.addComponents(infoObjects, modules, true);
		creator.addServiceDependencies(infoObjects, serviceDependencies, services);
		creator.addSubystemDependencies(infoObjects, serviceDependencies, subsys);
		creator.addSystemDependencies(infoObjects, serviceDependencies, system);
		systems.getSystem().addAll(system);

		for (DependencyPlacement placement : placeForRestDependency) {
			switch (placement) {
			case COMPONENT:
				creator.addComponentRestDependencies(infoObjects, serviceDependencies, components, true);
				break;
			case MODULE:
				creator.addModuleRestDependencies(infoObjects, serviceDependencies, modules, true);
				break;
			case SERVICE:
				creator.addServiceRestDependencies(infoObjects, serviceDependencies, services, true);
				break;
			case SUBSYSTEM:
				creator.addSubystemRestDependencies(infoObjects, serviceDependencies, subsys, true);
				break;
			case SYSTEM:
				creator.addSystemRestDependencies(infoObjects, serviceDependencies, system, true);
				break;
			default:
				break;
			}
		}
		
		if (!placeForRestDependency.contains(DependencyPlacement.COMPONENT)) {
			creator.addComponentRestDependencies(infoObjects, serviceDependencies, components, false);
		}
		if (!placeForRestDependency.contains(DependencyPlacement.MODULE)) {
			creator.addModuleRestDependencies(infoObjects, serviceDependencies, modules, false);
		}
		if (!placeForRestDependency.contains(DependencyPlacement.SERVICE)) {
			creator.addServiceRestDependencies(infoObjects, serviceDependencies, services, false);
		}
		if (!placeForRestDependency.contains(DependencyPlacement.SUBSYSTEM)) {
			creator.addSubystemRestDependencies(infoObjects, serviceDependencies, subsys, false);
		}
		if (!placeForRestDependency.contains(DependencyPlacement.SYSTEM)) {
			creator.addSystemRestDependencies(infoObjects, serviceDependencies, system, false);
		}
		
		return systems;
	}

	public enum DependencyPlacement {
		COMPONENT, MODULE, SERVICE, SUBSYSTEM, SYSTEM;
	};

}
