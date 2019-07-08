package business.generator.impl.generators;

import data.model.xml.Dependencies;
import data.model.xml.Module;
import data.model.xml.Service;
import data.model.xml.Subsystem;
import data.model.xml.System;
import data.model.xml.Systems;

public class SystemsCustomizer {

	public Systems getModuleDiagram(Systems systems) {
		for (System system : systems.getSystem()) {
			system.setSystemDependencies(new Dependencies());
			for (Subsystem subsys : system.getSubsystem()) {
				subsys.setSubsystemDependencies(new Dependencies());
				for (Service service : subsys.getService()) {
					service.setServiceDependencies(new Dependencies());
					for (Module module : service.getModule()) {
						module.getComponent().clear();
					}
				}
			}
		}
		return systems;
	}

	public Systems getComponentModuleDiagram(Systems systems) {
		for (System system : systems.getSystem()) {
			system.setSystemDependencies(new Dependencies());
			for (Subsystem subsys : system.getSubsystem()) {
				subsys.setSubsystemDependencies(new Dependencies());
				for (Service service : subsys.getService()) {
					service.setServiceDependencies(new Dependencies());
					for (Module module : service.getModule()) {
						module.setModuleDependencies(new Dependencies());
					}
				}
			}
		}
		return systems;
	}

	public Systems getServicesWithRestDiagram(Systems systems) {

		for (System system : systems.getSystem()) {
			system.setSystemDependencies(new Dependencies());
			for (Subsystem subsys : system.getSubsystem()) {
				subsys.setSubsystemDependencies(new Dependencies());
				for (Service service : subsys.getService()) {
					service.getComponent().clear();
					service.getModule().clear();
					if (service.getServiceDependencies() != null) {
						service.getServiceDependencies().getDependency().clear();
					}
				}
			}
		}
		return systems;
	}

}
