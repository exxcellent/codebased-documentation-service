package business.converter;

import java.util.ArrayList;
import java.util.List;

import data.model.xml.Component;
import data.model.xml.Dependencies;
import data.model.xml.Module;
import data.model.xml.RestDependency;
import data.model.xml.Service;
import data.model.xml.Subsystem;
import data.model.xml.System;
import data.model.xml.Systems;

public class PlantUMLStringConverter {

	private static String BEGIN_DIAGRAM = "@startuml\n skinparam componentStyle uml2\n\n";
	private static String END_DIAGRAM = "@enduml\n";
	
	public String convertSystems(System system) {
		String diagramDescription = BEGIN_DIAGRAM;
		
		List<String> dependencyStrings = new ArrayList<>();
		
		diagramDescription += "package \"" + "system: " + system.getName() + "\" { \n";
		diagramDescription += getSubsystems(system, dependencyStrings);
		diagramDescription += "}\n\n";
		
		for (String depString : dependencyStrings) {
			diagramDescription += depString;
		}
		diagramDescription += END_DIAGRAM;
		return diagramDescription;
	}

	public String convertSystems(List<System> systems) {
		String diagramDescription = BEGIN_DIAGRAM;

		List<String> dependencyStrings = new ArrayList<>();
		for (System currentSystem : systems) {
			diagramDescription += "package \"" + "system: " + currentSystem.getName() + "\" { \n";
			diagramDescription += getSubsystems(currentSystem, dependencyStrings);
			diagramDescription += "}\n\n";
			addDependenciesBetweenPackages(currentSystem.getName(), currentSystem.getSystemDependencies(),
					dependencyStrings);
		}

		for (String depString : dependencyStrings) {
			diagramDescription += depString;
		}

		return diagramDescription + END_DIAGRAM;
	}

	private String getSubsystems(System system, List<String> dependencyStrings) {
		String description = "";

		if (system.getSubsystem() != null) {
			for (Subsystem subsystem : system.getSubsystem()) {
				description += "package \"" + "subsystem: " + subsystem.getName() + "\" { \n";

				if (subsystem.getService() != null) {
					description += getServices(subsystem, dependencyStrings);
				}

				description += "}\n\n";

				addDependenciesBetweenPackages(subsystem.getName(), subsystem.getSubsystemDependencies(),
						dependencyStrings);
			}

			return description;
		} else {
			return description;
		}
	}

	private String getServices(Subsystem subsystem, List<String> dependencyStrings) {
		String description = "";

		if (subsystem.getService() != null) {
			boolean isPackage = false;
			for (Service service : subsystem.getService()) {
				if ((service.getModule() != null && !service.getModule().isEmpty())|| (service.getComponent() != null && !service.getComponent().isEmpty())) {
					isPackage = true;
					break;
				}
			}
			for (Service service : subsystem.getService()) {
				if (isPackage) {
					description += "package \"" + "service: " + service.getName() + "\" { \n";

					if (service.getModule() != null) {
						description += getModules(service, dependencyStrings);
					} else if (service.getComponent() != null) {
						description += getComponents(service, dependencyStrings);
					}

					description += "}\n\n";
					addDependenciesBetweenPackages(service.getName(), service.getServiceDependencies(), dependencyStrings);
				} else {
					description += "[\"" + service.getName() + "\"] \n";
					addDependenciesBetweenComponents(service.getName(), service.getServiceDependencies(),
							dependencyStrings);
				}
			}

			return description;
		} else {
			return description;
		}
	}

	private String getComponents(Service service, List<String> dependencyStrings) {
		String description = "";
		if (service.getComponent() != null) {
			for (Component component : service.getComponent()) {
				description += "[\"" + component.getName() + "\"]\n";
				addDependenciesBetweenComponents(component.getName(), component.getComponentDependencies(),
						dependencyStrings);
			}
			return description;
		} else {
			return description;
		}
	}

	private String getModules(Service service, List<String> dependencyStrings) {
		String description = "";

		if (service.getModule() != null) {
			boolean isPackage = false;
			for (Module module : service.getModule()) {
				if (module.getComponent() != null && !module.getComponent().isEmpty()) {
					isPackage = true;
					break;
				}
			}
			for (Module module : service.getModule()) {
				if (isPackage) {
					description += "package \"" + "module: " + module.getName() + "\" { \n";
					
					if (module.getComponent() != null) {
						description += getComponents(module, dependencyStrings);
					}
					
					description += "}\n\n";
					addDependenciesBetweenPackages(module.getName(), module.getModuleDependencies(), dependencyStrings);
				} else {
					description += "[\"" + module.getName() + "\"]\n";
					addDependenciesBetweenComponents(module.getName(), module.getModuleDependencies(), dependencyStrings);
				}
			}

			return description;
		} else {
			return description;
		}
	}

	private String getComponents(Module module, List<String> dependencyStrings) {
		String description = "";

		if (module.getComponent() != null) {
			for (Component component : module.getComponent()) {
				description += "[\"" + component.getName() + "\"] \n";

				addDependenciesBetweenComponents(component.getName(), component.getComponentDependencies(),
						dependencyStrings);
			}
			return description;
		} else {
			return description;
		}
	}

	private void addDependenciesBetweenPackages(String name, Dependencies dependencies,
			List<String> dependencyStrings) {
		if (dependencies != null) {
			for (String dependency : dependencies.getDependency()) {
				dependencyStrings.add("\"" + name + "\"" + " ..> " + "\"" + dependency + "\" : use \n");
			}
			
			for (RestDependency restDependency : dependencies.getRestDependency()) {
				String usage = "\"" + restDependency.getMethod() + ": " + restDependency.getPath() + "\"";
				dependencyStrings.add("\"" + name + "\"" + " --> " + "\"" + restDependency.getCalls() + "\" : " + usage + " \n");				
			}
		}
	}

	private void addDependenciesBetweenComponents(String name, Dependencies dependencies,
			List<String> dependencyStrings) {
		if (dependencies != null) {
			for (String dependency : dependencies.getDependency()) {
				dependencyStrings.add("[\"" + name + "\"]" + " ..> " + "[\"" + dependency + "\"] : use \n");
			}
			
			for (RestDependency restDependency : dependencies.getRestDependency()) {
				String usage = "\"" + restDependency.getMethod() + ": " + restDependency.getPath() + "\"";
				dependencyStrings.add("[\"" + name + "\"]" + " --> " + "[\"" + restDependency.getCalls() + "\"] : " + usage + " \n");				
			}
		}
	}
	

}
