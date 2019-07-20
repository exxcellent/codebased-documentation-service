package business.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import data.model.xml.Component;
import data.model.xml.Dependencies;
import data.model.xml.Module;
import data.model.xml.RestDependency;
import data.model.xml.Service;
import data.model.xml.Subsystem;
import data.model.xml.System;

public class XMLToPlantUMLConverter {

	private static String BEGIN_DIAGRAM = "@startuml\n skinparam componentStyle uml2\n\n";
	private static String END_DIAGRAM = "@enduml\n";

	private Map<String, String> serviceTagToName;
	private Map<String, String> moduleTagToName;
	
	private Map<String, String> serviceNameAndinterfaceToAlias = new HashMap<>();
	private int interfaceCounter = 0;

	public String convertSystems(System system) {
		serviceNameAndinterfaceToAlias = new HashMap<>();
		serviceTagToName = null;
		if (!areThereDoubleServiceNames(ImmutableList.of(system))) {
			serviceTagToName = getTagToNameForServices(Lists.newArrayList(system));
		}
		moduleTagToName = null;
		if (!areThereDoubleModuleNames(ImmutableList.of(system))) {
			moduleTagToName = getTagToNameForModules(Lists.newArrayList(system));
		}

		String diagramDescription = BEGIN_DIAGRAM;

		List<String> dependencyStrings = new ArrayList<>();
		List<String> interfaces = new ArrayList<>();

		diagramDescription += "package \"" + "system: " + system.getName() + "\" { \n";
		diagramDescription += getSubsystems(system, dependencyStrings, interfaces);
		diagramDescription += "}\n\n";

		for (String currentInterface : interfaces) {
			diagramDescription += currentInterface;
		}
		for (String depString : dependencyStrings) {
			diagramDescription += depString;
		}
		interfaceCounter = 0;
		diagramDescription += END_DIAGRAM;
		return diagramDescription;
	}

	public String convertSystems(List<System> systems) {
		serviceNameAndinterfaceToAlias = new HashMap<>();
		serviceTagToName = null;
		if (!areThereDoubleServiceNames(systems)) {
			serviceTagToName = getTagToNameForServices(systems);
		}
		moduleTagToName = null;
		if (!areThereDoubleModuleNames(systems)) {
			moduleTagToName = getTagToNameForModules(systems);
		}

		String diagramDescription = BEGIN_DIAGRAM;

		List<String> dependencyStrings = new ArrayList<>();
		List<String> interfaces = new ArrayList<>();
		for (System currentSystem : systems) {
			diagramDescription += "package \"" + "system: " + currentSystem.getName() + "\" { \n";
			diagramDescription += getSubsystems(currentSystem, dependencyStrings, interfaces);
			diagramDescription += "}\n\n";
			addDependenciesBetweenPackages(currentSystem.getName(), "system", currentSystem.getSystemDependencies(), false, false,
					dependencyStrings, interfaces);
		}

		for (String currentInterface : interfaces) {
			diagramDescription += currentInterface;
		}
		for (String depString : dependencyStrings) {
			diagramDescription += depString;
		}

		interfaceCounter = 0;
		return diagramDescription + END_DIAGRAM;
	}

	private boolean areThereDoubleServiceNames(List<System> systems) {
		List<String> allNames = new ArrayList<>();
		for (System system : systems) {
			if (system.getSubsystem() != null) {
				for (Subsystem subsystem : system.getSubsystem()) {
					if (subsystem.getService() != null) {
						for (Service service : subsystem.getService()) {
							if (allNames.contains(service.getName())) {
								return true;
							} else {
								allNames.add(service.getName());
							}
						}
					}
				}
			}
		}

		return false;
	}

	private boolean areThereDoubleModuleNames(List<System> systems) {
		List<String> allNames = new ArrayList<>();
		for (System system : systems) {
			if (system.getSubsystem() != null) {
				for (Subsystem subsystem : system.getSubsystem()) {
					if (subsystem.getService() != null) {
						for (Service service : subsystem.getService()) {
							if (service.getModule() != null) {
								for (Module module : service.getModule()) {
									if (allNames.contains(module.getName())) {
										return true;
									} else {
										allNames.add(module.getName());
									}
								}
							}
						}
					}
				}
			}
		}

		return false;
	}

	private String getSubsystems(System system, List<String> dependencyStrings, List<String> interfaces) {
		String description = "";

		if (system.getSubsystem() != null) {
			for (Subsystem subsystem : system.getSubsystem()) {
				description += "package \"" + "subsystem: " + subsystem.getName() + "\" { \n";

				if (subsystem.getService() != null) {
					description += getServices(subsystem, dependencyStrings, interfaces);
				}

				description += "}\n\n";

				addDependenciesBetweenPackages(subsystem.getName(), "subsystem", subsystem.getSubsystemDependencies(), false, false,
						dependencyStrings, interfaces);
			}

			return description;
		} else {
			return description;
		}
	}

	private String getServices(Subsystem subsystem, List<String> dependencyStrings, List<String> interfaces) {
		String description = "";

		if (subsystem.getService() != null) {
			boolean isPackage = false;
			for (Service service : subsystem.getService()) {
				if ((service.getModule() != null && !service.getModule().isEmpty())
						|| (service.getComponent() != null && !service.getComponent().isEmpty())) {
					isPackage = true;
					break;
				}
			}
			for (Service service : subsystem.getService()) {
				String serviceDisplayName = ((serviceTagToName != null) ? service.getName() : service.getTag());
				if (isPackage) {
					description += "package \"" + "service: " + serviceDisplayName + "\" { \n";

					if (service.getModule() != null) {
						description += getModules(service, dependencyStrings, interfaces);
					} else if (service.getComponent() != null) {
						description += getComponents(service, dependencyStrings, interfaces);
					}

					description += "}\n\n";
					addDependenciesBetweenPackages(serviceDisplayName, "service", service.getServiceDependencies(), true, false,
							dependencyStrings, interfaces);
				} else {
					description += "[\"" + serviceDisplayName + "\"] \n";
					addDependenciesBetweenComponents(serviceDisplayName, service.getServiceDependencies(), true, false,
							dependencyStrings, interfaces);
				}
			}

			return description;
		} else {
			return description;
		}
	}

	private String getComponents(Service service, List<String> dependencyStrings, List<String> interfaces) {
		String description = "";
		if (service.getComponent() != null) {
			for (Component component : service.getComponent()) {
				description += "[\"" + component.getName() + "\"]\n";
				addDependenciesBetweenComponents(component.getName(), component.getComponentDependencies(), false, false,
						dependencyStrings, interfaces);
			}
			return description;
		} else {
			return description;
		}
	}

	private String getModules(Service service, List<String> dependencyStrings, List<String> interfaces) {
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
				String moduleDisplayName = ((moduleTagToName != null) ? module.getName() : module.getTag());
				if (isPackage) {
					description += "package \"" + "module: " + moduleDisplayName + "\" { \n";

					if (module.getComponent() != null) {
						description += getComponents(module, dependencyStrings, interfaces);
					}

					description += "}\n\n";
					addDependenciesBetweenPackages(moduleDisplayName, "module", module.getModuleDependencies(), false, true, 
							dependencyStrings, interfaces);
				} else {
					description += "[\"" + moduleDisplayName + "\"]\n";
					addDependenciesBetweenComponents(moduleDisplayName, module.getModuleDependencies(), false, true, 
							dependencyStrings, interfaces);
				}
			}

			return description;
		} else {
			return description;
		}
	}

	private String getComponents(Module module, List<String> dependencyStrings, List<String> interfaces) {
		String description = "";

		if (module.getComponent() != null) {
			for (Component component : module.getComponent()) {
				description += "[\"" + component.getName() + "\"] \n";

				addDependenciesBetweenComponents(component.getName(), component.getComponentDependencies(), false, false,
						dependencyStrings, interfaces);
			}
			return description;
		} else {
			return description;
		}
	}

	private Map<String, String> getTagToNameForServices(List<System> systems) {
		Map<String, String> tagToName = new HashMap<>();
		for (System system : systems) {
			for (Subsystem subsystem : system.getSubsystem()) {
				for (Service service : subsystem.getService()) {
					tagToName.put(service.getTag(), service.getName());
				}
			}
		}
		return tagToName;
	}

	private Map<String, String> getTagToNameForModules(List<System> systems) {
		Map<String, String> tagToName = new HashMap<>();
		for (System system : systems) {
			for (Subsystem subsystem : system.getSubsystem()) {
				for (Service service : subsystem.getService()) {
					for (Module module : service.getModule()) {
						tagToName.put(module.getTag(), module.getName());
					}
				}
			}
		}
		return tagToName;
	}

	
	private void addDependenciesBetweenPackages(String name, String type, Dependencies dependencies, boolean service, boolean module,
			List<String> dependencyStrings, List<String> interfaces) {
		if (dependencies != null) {

			for (String dependency : dependencies.getDependency()) {
				dependencyStrings.add("\"" + type + ": " + name + "\"" + " ..> " + "\"" + type + ": " + getDependencyDisplayString(service, module, dependency) + "\" : <<use>> \n");
			}
			for (RestDependency restDependency : dependencies.getRestDependency()) {
				
				String serviceAndInterface = restDependency.getCalls() + "_" + restDependency.getPath();
				String interfaceAlias;
				if (serviceNameAndinterfaceToAlias.get(serviceAndInterface) == null) {
					interfaceCounter++;
					interfaceAlias = "I" + interfaceCounter;
					serviceNameAndinterfaceToAlias.put(serviceAndInterface, interfaceAlias);
					
					String newInterface = "() \"" + restDependency.getPath() + "\" as " + interfaceAlias + "\n";
					interfaces.add(newInterface);
					
					String toInterface = "\"" + type + ": " + getDependencyDisplayString(service, module, restDependency.getCalls()) + "\"" + " -- " + interfaceAlias+ " \n";
					dependencyStrings.add(toInterface);
				} else {
					interfaceAlias = serviceNameAndinterfaceToAlias.get(serviceAndInterface);
				}
				
				String useInterface = "\"" + type + ": " + name + "\"" + " --( " + interfaceAlias + " : <<" + restDependency.getMethod() + ">>\n";
				
				dependencyStrings.add(useInterface);
			}
		}
	}

	private void addDependenciesBetweenComponents(String name, Dependencies dependencies,
			boolean service, boolean module, List<String> dependencyStrings, List<String> interfaces) {
		if (dependencies != null) {
			for (String dependency : dependencies.getDependency()) {
				dependencyStrings.add("[\"" + name + "\"]" + " ..> " + "[\"" + getDependencyDisplayString(service, module, dependency) + "\"] : <<use>> \n");
			}

			for (RestDependency restDependency : dependencies.getRestDependency()) {
				
				String serviceAndInterface = restDependency.getCalls() + "_" + restDependency.getPath();
				String interfaceAlias;
				if (serviceNameAndinterfaceToAlias.get(serviceAndInterface) == null) {
					interfaceCounter++;
					interfaceAlias = "I" + interfaceCounter;
					serviceNameAndinterfaceToAlias.put(serviceAndInterface, interfaceAlias);
					
					String newInterface = "() \"" + restDependency.getPath() + "\" as " + interfaceAlias+ "\n";
					interfaces.add(newInterface);
					
					String toInterface = "[\"" + getDependencyDisplayString(service, module, restDependency.getCalls()) + "\"]" + " -- " + interfaceAlias + "\n";
					dependencyStrings.add(toInterface);
				} else {
					interfaceAlias = serviceNameAndinterfaceToAlias.get(serviceAndInterface);
				}
				
								
				String useInterface = "[\"" + name + "\"]" + " --( " + interfaceAlias + " : <<" + restDependency.getMethod() + ">>\n";
				dependencyStrings.add(useInterface);
			}
		}
	}

	private String getDependencyDisplayString(boolean service, boolean module, String dependency) {
		if (dependency == null || dependency.isEmpty()) {
			java.lang.System.out.println("DEPENDENCY:" + dependency);
		}
		if (service && serviceTagToName != null) { //current dependency on service level, there were no double names in services
			String result = serviceTagToName.get(dependency);
			return (result == null) ? dependency : result;
		} else if (module && moduleTagToName != null) { //current dependency on module level, there were no double names in modules
			String result = moduleTagToName.get(dependency);
			return (result == null) ? dependency : result;
		}
		return dependency;
	}

}
