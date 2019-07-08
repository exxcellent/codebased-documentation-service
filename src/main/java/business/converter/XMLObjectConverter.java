package business.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;

import business.model.Dependency;
import collectors.models.maven.CollectedMavenInfoObject;
import collectors.models.maven.ComponentInfoObject;
import collectors.models.maven.ModuleInfoObject;
import collectors.models.maven.ModuleToComponentInfoObject;
import data.model.xml.Component;
import data.model.xml.Dependencies;
import data.model.xml.Module;
import data.model.xml.RestDependency;
import data.model.xml.Service;
import data.model.xml.Subsystem;
import data.model.xml.System;

/**
 * Turns CollectedMavenInfoObjects into Objects of the classes that were
 * generated through the xsd files.
 * 
 * @author gmittmann
 *
 */
public class XMLObjectConverter {

	/**
	 * Gets all systems defined in the given InfoObjects as generated System
	 * objects. If a system is defined twice or more times, it is still returned
	 * once.
	 * 
	 * @param infoObjects defining CollectedMavenInfoObjects.
	 * @return List of found Systems.
	 */
	public List<System> getSystems(List<CollectedMavenInfoObject> infoObjects) {
		Set<System> systemList = new HashSet<>();
		for (CollectedMavenInfoObject infoObject : infoObjects) {
			System sys = new System();
			sys.setName(infoObject.getSystem());
			systemList.add(sys);
		}
		return Lists.newArrayList(systemList);
	}

	/**
	 * Adds the dependencies between systems to the System objects.
	 * 
	 * @param infoObjects defining CollectedMavenInfoObject.
	 * @param systems     systems to add dependencies to.
	 * @return the systems with the dependencies added.
	 */
	public List<System> addSystemDependencies(List<CollectedMavenInfoObject> infoObjects,
			List<Dependency> serviceDependencies, List<System> systems) {
		// TODO: implement when infoObject contains this info.
		Map<String, String> serviceToSystem = mapServiceToSystem(infoObjects);

		for (Dependency serviceDependency : serviceDependencies) {
			String currentSystem = serviceToSystem.get(serviceDependency.getService());
			String systemOfDependsOn = serviceToSystem.get(serviceDependency.getDependsOn());
			if (systemOfDependsOn == null) {
				systemOfDependsOn = "ext";
			}
			if (!currentSystem.equalsIgnoreCase(systemOfDependsOn)) {
				for (System system : systems) {
					if (system.getName().equals(currentSystem)) {
						if (system.getSystemDependencies() == null) {
							Dependencies dependencies = new Dependencies();
							dependencies.getDependency().add(systemOfDependsOn);
							system.setSystemDependencies(dependencies);
						} else {
							if (!system.getSystemDependencies().getDependency().contains(systemOfDependsOn)) {
								system.getSystemDependencies().getDependency().add(systemOfDependsOn);
							}
						}
					}
				}
			}
		}

		return systems;
	}

	/**
	 * Adds the dependencies between systems to the System objects.
	 * 
	 * @param infoObjects defining CollectedMavenInfoObject.
	 * @param systems     systems to add dependencies to.
	 * @return the systems with the dependencies added.
	 */
	public List<System> addSystemRestDependencies(List<CollectedMavenInfoObject> infoObjects,
			List<Dependency> serviceDependencies, List<System> systems, boolean asRestDependency) {

		Map<String, String> serviceToSystem = mapServiceToSystem(infoObjects);

		for (Dependency serviceDependency : serviceDependencies) {
			String currentSystem = serviceToSystem.get(serviceDependency.getService());
			String systemOfDependsOn = serviceToSystem.get(serviceDependency.getDependsOn());
			String path = serviceDependency.getPath();
			String method = serviceDependency.getMethod();
			if (systemOfDependsOn == null) {
				systemOfDependsOn = "ext";
			}
			if (!currentSystem.equalsIgnoreCase(systemOfDependsOn)) {
				for (System system : systems) {
					if (system.getName().equals(currentSystem)) {
						if (system.getSystemDependencies() == null) {
							Dependencies dependencies = new Dependencies();
							system.setSystemDependencies(dependencies);
						}

						if (asRestDependency) {
							RestDependency restDependency = new RestDependency();
							restDependency.setMethod(method);
							restDependency.setPath(path);
							restDependency.setCalls(systemOfDependsOn);
							system.getSystemDependencies().getRestDependency().add(restDependency);

						} else {
							if (!system.getSystemDependencies().getDependency().contains(systemOfDependsOn)) {
								system.getSystemDependencies().getDependency().add(systemOfDependsOn);
							}
						}
					}
				}
			}
		}

		return systems;
	}

	private Map<String, String> mapServiceToSystem(List<CollectedMavenInfoObject> infoObjects) {
		Map<String, String> serviceToSystem = new HashMap<>();

		for (CollectedMavenInfoObject infoO : infoObjects) {
			serviceToSystem.put(infoO.getProjectName(), infoO.getSystem());
		}

		return serviceToSystem;
	}

	/**
	 * Add subsystems defined in the CollectedMavenInfoObjects to the given systems.
	 * 
	 * @param infoObjects defining CollectedMavenInfoObjects.
	 * @param systems     System objects, to which subsystems are to be added.
	 * @return All subsystems that were created.
	 */
	public List<Subsystem> addSubsystems(List<CollectedMavenInfoObject> infoObjects, List<System> systems) {
		List<Subsystem> allSubsystems = new ArrayList<>();
		for (System currentSystem : systems) {
			Set<Subsystem> subsystems = new HashSet<>();
			for (CollectedMavenInfoObject infoObject : infoObjects) {
				if (infoObject.getSystem().equals(currentSystem.getName())) {
					Subsystem sub = new Subsystem();
					sub.setName(infoObject.getSubsystem());
					subsystems.add(sub);
				}
			}
			currentSystem.getSubsystem().addAll(subsystems);
			allSubsystems.addAll(subsystems);
		}
		return allSubsystems;
	}

	/**
	 * Gets all subsystems defined in the given InfoObjects as generated Subsystem
	 * objects. If a subsystem is defined twice or more times, it is still returned
	 * once.
	 * 
	 * @param infoObjects defining CollectedMavenInfoObjects.
	 * @return List of found Subsystems.
	 */
	public List<Subsystem> getSubsystems(List<CollectedMavenInfoObject> infoObjects) {
		Set<Subsystem> subsystems = new HashSet<>();

		for (CollectedMavenInfoObject infoObject : infoObjects) {
			Subsystem subsystem = new Subsystem();
			subsystem.setName(infoObject.getSubsystem());
			subsystems.add(subsystem);
		}

		return Lists.newArrayList(subsystems);
	}

	/**
	 * Adds the dependencies between subsystems to the Subsystem objects.
	 * 
	 * @param infoObjects defining CollectedMavenInfoObject.
	 * @param subsystems  subsystems to add dependencies to.
	 * @return the subsystems with the dependencies added.
	 */
	public List<Subsystem> addSubystemDependencies(List<CollectedMavenInfoObject> infoObjects,
			List<Dependency> serviceDependencies, List<Subsystem> subsystems) {

		Map<String, String> serviceToSubsystem = mapServiceToSubsystem(infoObjects);

		for (Dependency serviceDependency : serviceDependencies) {
			String currentSubsystem = serviceToSubsystem.get(serviceDependency.getService());
			String subsystemOfDependsOn = serviceToSubsystem.get(serviceDependency.getDependsOn());
			if (subsystemOfDependsOn == null) {
				subsystemOfDependsOn = "ext";
			}

			if (!currentSubsystem.equalsIgnoreCase(subsystemOfDependsOn)) {
				for (Subsystem subsystem : subsystems) {
					if (subsystem.getName().equals(currentSubsystem)) {
						if (subsystem.getSubsystemDependencies() == null) {
							Dependencies dependencies = new Dependencies();
							dependencies.getDependency().add(subsystemOfDependsOn);
							subsystem.setSubsystemDependencies(dependencies);
						} else {
							if (!subsystem.getSubsystemDependencies().getDependency().contains(subsystemOfDependsOn)) {
								subsystem.getSubsystemDependencies().getDependency().add(subsystemOfDependsOn);
							}
						}
					}
				}
			}
		}

		return subsystems;
	}

	/**
	 * Adds the dependencies between subsystems to the Subsystem objects.
	 * 
	 * @param infoObjects defining CollectedMavenInfoObject.
	 * @param subsystems  subsystems to add dependencies to.
	 * @return the subsystems with the dependencies added.
	 */
	public List<Subsystem> addSubystemRestDependencies(List<CollectedMavenInfoObject> infoObjects,
			List<Dependency> serviceDependencies, List<Subsystem> subsystems, boolean asRestDependency) {

		Map<String, String> serviceToSubsystem = mapServiceToSubsystem(infoObjects);

		for (Dependency serviceDependency : serviceDependencies) {
			String currentSubsystem = serviceToSubsystem.get(serviceDependency.getService());
			String subsystemOfDependsOn = serviceToSubsystem.get(serviceDependency.getDependsOn());
			String path = serviceDependency.getPath();
			String method = serviceDependency.getMethod();
			if (subsystemOfDependsOn == null) {
				subsystemOfDependsOn = "ext";
			}

			if (!currentSubsystem.equalsIgnoreCase(subsystemOfDependsOn)) {
				for (Subsystem subsystem : subsystems) {
					if (subsystem.getName().equals(currentSubsystem)) {
						if (subsystem.getSubsystemDependencies() == null) {
							Dependencies dependencies = new Dependencies();
							subsystem.setSubsystemDependencies(dependencies);
						}

						if (asRestDependency) {
							RestDependency restDependency = new RestDependency();
							restDependency.setMethod(method);
							restDependency.setPath(path);
							restDependency.setCalls(subsystemOfDependsOn);
							subsystem.getSubsystemDependencies().getRestDependency().add(restDependency);
						} else {
							if (!subsystem.getSubsystemDependencies().getDependency().contains(subsystemOfDependsOn)) {
								subsystem.getSubsystemDependencies().getDependency().add(subsystemOfDependsOn);
							}
						}

					}
				}
			}
		}

		return subsystems;
	}

	private Map<String, String> mapServiceToSubsystem(List<CollectedMavenInfoObject> infoObjects) {
		Map<String, String> serviceToSubsystem = new HashMap<>();

		for (CollectedMavenInfoObject infoO : infoObjects) {
			serviceToSubsystem.put(infoO.getProjectName(), infoO.getSubsystem());
		}

		return serviceToSubsystem;
	}

	/**
	 * Add services defined in the CollectedMavenInfoObjects to the given
	 * subsystems.
	 * 
	 * @param infoObjects defining CollectedMavenInfoObjects.
	 * @param subsystems  Subsystem objects, to which services are to be added.
	 * @return All services that were created.
	 */
	public List<Service> addServices(List<CollectedMavenInfoObject> infoObjects, List<Subsystem> subsystems) {
		List<Service> allServices = new ArrayList<>();
		for (Subsystem subsystem : subsystems) {
			Set<Service> services = new HashSet<>();
			for (CollectedMavenInfoObject infoObject : infoObjects) {
				if (infoObject.getSubsystem().equals(subsystem.getName())) {
					Service service = new Service();
					service.setName(infoObject.getProjectName());
					services.add(service);
				}
			}
			subsystem.getService().addAll(services);
			allServices.addAll(services);
		}
		return allServices;
	}

	/**
	 * Gets all services defined in the given InfoObjects as generated Service
	 * objects. If a service is defined twice or more times, it is still returned
	 * once.
	 * 
	 * @param infoObjects      defining CollectedMavenInfoObjects.
	 * @param withDependencies if true, generates and fills the Dependencies Object
	 *                         for each of the services.
	 * @return List of found Systems.
	 */
	public List<Service> getServices(List<CollectedMavenInfoObject> infoObjects, List<Dependency> serviceDependencies,
			boolean withDependencies) {
		Set<Service> services = new HashSet<>();

		for (CollectedMavenInfoObject infoObject : infoObjects) {
			Service service = new Service();
			service.setName(infoObject.getProjectName());

			if (withDependencies) {
				for (Dependency dep : serviceDependencies) {
					if (dep.getService().equals(infoObject.getProjectName())) {
						if (service.getServiceDependencies() == null) {
							Dependencies dependencies = new Dependencies();
							dependencies.getDependency().add(dep.getDependsOn());
							service.setServiceDependencies(dependencies);
						}
						if (service.getServiceDependencies().getDependency().contains(dep.getDependsOn())) {
							service.getServiceDependencies().getDependency().add(dep.getDependsOn());
						}
					}
				}
			}

			services.add(service);
		}

		return Lists.newArrayList(services);
	}

	/**
	 * Adds the dependencies between services to the Service objects.
	 * 
	 * @param infoObjects defining CollectedMavenInfoObject.
	 * @param services    services to add dependencies to.
	 * @return the systems with the dependencies added.
	 */
	public List<Service> addServiceDependencies(List<CollectedMavenInfoObject> infoObjects,
			List<Dependency> serviceDependencies, List<Service> services) {

		for (Service currentService : services) {
			for (Dependency dependency : serviceDependencies) {
				if (currentService.getName().equals(dependency.getService())) {
					if (currentService.getServiceDependencies() == null) {
						Dependencies dep = new Dependencies();
						dep.getDependency().add(dependency.getDependsOn());
						currentService.setServiceDependencies(dep);
					} else {
						if (!currentService.getServiceDependencies().getDependency()
								.contains(dependency.getDependsOn())) {
							currentService.getServiceDependencies().getDependency().add(dependency.getDependsOn());
						}
					}
				}
			}
		}
		return services;
	}

	public List<Service> addServiceRestDependencies(List<CollectedMavenInfoObject> infoObjects,
			List<Dependency> serviceDependencies, List<Service> services, boolean asRestDependency) {

		for (Service currentService : services) {
			for (Dependency dependency : serviceDependencies) {
				String path = dependency.getPath();
				String method = dependency.getMethod();
				if (currentService.getName().equals(dependency.getService())) {
					if (currentService.getServiceDependencies() == null) {
						Dependencies dep = new Dependencies();
						currentService.setServiceDependencies(dep);
					}

					if (asRestDependency) {
						RestDependency restDependency = new RestDependency();
						restDependency.setCalls(dependency.getDependsOn());
						restDependency.setPath(path);
						restDependency.setMethod(method);
						currentService.getServiceDependencies().getRestDependency().add(restDependency);
					} else {
						if (!currentService.getServiceDependencies().getDependency().contains(dependency.getService())) {
							currentService.getServiceDependencies().getDependency().add(dependency.getService());
						}
					}

				}
			}
		}
		return services;
	}

	/**
	 * Add modules defined in the CollectedMavenInfoObjects to the given service.
	 * 
	 * DO NOT ADD COMPONENTS TO THE SERVICES after this method was called. If you
	 * want components and modules added to the service, add modules first, then add
	 * the components to the modules (not to the services).
	 * 
	 * @param infoObjects      defining CollectedMavenInfoObjects.
	 * @param services         Service objects, to which modules are to be added.
	 * @param withDependencies if true, dependencies are added to the created
	 *                         modules.
	 * @return All modules that were created.
	 */
	public List<Module> addModules(List<CollectedMavenInfoObject> infoObjects, List<Service> services,
			boolean withDependencies) {
		List<Module> allModules = new ArrayList<>();
		for (Service service : services) {
			Set<Module> modules = new HashSet<>();
			for (CollectedMavenInfoObject infoObject : infoObjects) {
				if (infoObject.getProjectName().equals(service.getName())) {
					for (ModuleInfoObject moduleInfo : infoObject.getModules()) {
						Module module = new Module();
						module.setName(moduleInfo.getModuleName());
						module.setTag(moduleInfo.getTag());

						if (withDependencies) {
							Dependencies deps = new Dependencies();
							for (String tag : moduleInfo.getDependsOn()) {
								deps.getDependency().add(getModuleNameFromTag(infoObject, tag));
							}
							module.setModuleDependencies(deps);
						}

						modules.add(module);
					}
				}
			}
			service.getModule().addAll(modules);
			allModules.addAll(modules);
		}
		return allModules;
	}
//
//	public List<Module> addModules(List<CollectedMavenInfoObject> infoObjects, List<Dependency> serviceDependencies,
//			List<Service> services, boolean withRestDependencies) {
//		
//		List<Module> allModules = new ArrayList<>();
//		for (Service service : services) {
//			Set<Module> modules = new HashSet<>();
//			for (CollectedMavenInfoObject infoObject : infoObjects) {
//				if (infoObject.getProjectName().equals(service.getName())) {
//					for (ModuleInfoObject moduleInfo : infoObject.getModules()) {
//						Module module = new Module();
//						module.setName(moduleInfo.getModuleName());
//						module.setTag(moduleInfo.getTag());
//
//						if (withRestDependencies) {
//							Dependencies deps = new Dependencies();
//							for (String tag : moduleInfo.getDependsOn()) {
//								deps.getDependency().add(getModuleNameFromTag(infoObject, tag));
//							}
//							module.setModuleDependencies(deps);
//						}
//
//						modules.add(module);
//					}
//				}
//			}
//			service.getModule().addAll(modules);
//			allModules.addAll(modules);
//		}
//		
//		if (withRestDependencies) {
//			for (Dependency dependency : serviceDependencies) {
//				
//			}
//		}
//		return allModules;
//	}

	/**
	 * Add components defined in the CollectedMavenInfoObjects to the given
	 * services.
	 * 
	 * DO NOT ADD MODULES to the services after this method was called. Use this
	 * method only, if you want just components and no modules. If you want
	 * components and modules added to the service, add modules first, then add the
	 * components to the modules (not to the services).
	 * 
	 * @param infoObjects      defining CollectedMavenInfoObjects.
	 * @param services         Service objects, to which components are to be added.
	 * @param withDependencies if true, dependencies are added to the created
	 *                         components.
	 * @return All components that were created.
	 */
	public List<Component> addComponentsToService(List<CollectedMavenInfoObject> infoObjects, List<Service> services,
			boolean withDependencies) {
		List<Component> allComponents = new ArrayList<>();
		for (Service service : services) {
			Set<Component> components = new HashSet<>();
			for (CollectedMavenInfoObject infoObject : infoObjects) {
				if (infoObject.getProjectName().equals(service.getName())) {
					for (ModuleToComponentInfoObject componentsInfo : infoObject.getComponents()) {
						for (ComponentInfoObject pkgInfo : componentsInfo.getComponents()) {
							Component component = new Component();
							component.setName(pkgInfo.getPackageName());

							if (withDependencies) {
								Dependencies deps = new Dependencies();
								deps.getDependency().addAll(pkgInfo.getDependsOn());
								component.setComponentDependencies(deps);
							}

							components.add(component);
						}
					}
				}
			}
			service.getComponent().addAll(components);
			allComponents.addAll(components);
		}
		return allComponents;
	}

	/**
	 * Gets all modules defined in the given InfoObjects as generated Module
	 * objects. If a module is defined twice or more times, it is still returned
	 * once.
	 * 
	 * @param infoObjects      defining CollectedMavenInfoObjects.
	 * @param withDependencies if true, generates and fills the Dependencies Object
	 *                         for each of the services.
	 * @return List of found Modules.
	 */
	public List<Module> getModules(List<CollectedMavenInfoObject> infoObjects, boolean withDependencies) {
		Set<Module> modules = new HashSet<>();

		for (CollectedMavenInfoObject infoObject : infoObjects) {
			for (ModuleInfoObject moduleInfo : infoObject.getModules()) {
				Module module = new Module();
				module.setName(moduleInfo.getModuleName());
				module.setTag(moduleInfo.getTag());

				if (withDependencies) {
					Dependencies deps = new Dependencies();
					for (String tag : moduleInfo.getDependsOn()) {
						deps.getDependency().add(getModuleNameFromTag(infoObject, tag));
					}
				}

				modules.add(module);
			}
		}

		return Lists.newArrayList(modules);
	}

	/**
	 * Adds the dependencies between modules to the Module objects.
	 * 
	 * @param infoObjects defining CollectedMavenInfoObject.
	 * @param modules     modules to add dependencies to.
	 * @return the modules with the dependencies added.
	 */
	public List<Module> addModuleDependencies(List<CollectedMavenInfoObject> infoObjects, List<Module> modules) {

		for (CollectedMavenInfoObject infoObject : infoObjects) {
			for (ModuleInfoObject moduleInfo : infoObject.getModules()) {
				if (moduleInfo.getDependsOn() != null && !moduleInfo.getDependsOn().isEmpty()) {
					Module currentModule = getModule(modules, moduleInfo.getModuleName());
					Dependencies dep = new Dependencies();
					for (String tag : moduleInfo.getDependsOn()) {
						dep.getDependency().add(getModuleNameFromTag(infoObject, tag));
					}
					currentModule.setModuleDependencies(dep);
				}
			}
		}

		return modules;
	}

	/**
	 * Adds the dependencies between modules to the Module objects.
	 * 
	 * @param infoObjects defining CollectedMavenInfoObject.
	 * @param modules     modules to add dependencies to.
	 * @return the modules with the dependencies added.
	 */
	public List<Module> addModuleRestDependencies(List<CollectedMavenInfoObject> infoObjects,
			List<Dependency> serviceDependencies, List<Module> modules, boolean asRestDependency) {

		Map<String, String> componentToModule = getComponentToModule(infoObjects);
		for (Dependency dependency : serviceDependencies) {
			if (componentToModule.containsKey(dependency.getServicePackage())) {
				String moduleName = componentToModule.get(dependency.getServicePackage());
				Module currentModule = getModuleFromNames(moduleName, modules);

				String dependName = componentToModule.get(dependency.getDependsOnPackage());
				if (dependName == null) {
					break;
				}
				if (currentModule.getModuleDependencies() == null) {
					Dependencies dependencies = new Dependencies();
					currentModule.setModuleDependencies(dependencies);
				}

				if (asRestDependency) {
				RestDependency restDependency = new RestDependency();
				restDependency.setPath(dependency.getPath());
				restDependency.setMethod(dependency.getMethod());
				restDependency.setCalls(dependName);
				currentModule.getModuleDependencies().getRestDependency().add(restDependency);
				} else {
					if (!currentModule.getModuleDependencies().getDependency().contains(dependName)) {
						currentModule.getModuleDependencies().getDependency().add(dependName);
					}
				}

			}
		}

		return modules;
	}

	/**
	 * Add components defined in the CollectedMavenInfoObjects to the given modules.
	 * 
	 * @param infoObjects      defining CollectedMavenInfoObjects.
	 * @param modules          Module objects, to which components are to be added.
	 * @param withDependencies if true, dependencies are added to the created
	 *                         components.
	 * @return All components that were created.
	 */
	public List<Component> addComponents(List<CollectedMavenInfoObject> infoObjects, List<Module> modules,
			boolean withDependencies) {
		List<Component> allComponents = new ArrayList<>();

		for (CollectedMavenInfoObject infoObject : infoObjects) {
			for (ModuleToComponentInfoObject componentsInfo : infoObject.getComponents()) {
				Module currentMod = getModule(modules, componentsInfo.getModuleName());

				for (ComponentInfoObject pkgInfo : componentsInfo.getComponents()) {
					Component component = new Component();
					component.setName(pkgInfo.getPackageName());

					if (withDependencies) {
						Dependencies deps = new Dependencies();
						deps.getDependency().addAll(pkgInfo.getDependsOn());
						component.setComponentDependencies(deps);
					}

					currentMod.getComponent().add(component);
					allComponents.add(component);
				}
			}
		}

		return allComponents;
	}

	public List<Component> addExternalComponentDependencies(List<Component> components,
			List<Dependency> serviceDependencies) {

		for (Component component : components) {
			String pkgName = component.getName();
			for (Dependency serviceDep : serviceDependencies) {
				if (serviceDep.getServicePackage().startsWith(pkgName)) {
					String dependencyComponentName = serviceDep.getDependsOnPackage();
					if (!component.getComponentDependencies().getDependency().contains(dependencyComponentName)) {
						component.getComponentDependencies().getDependency().add(dependencyComponentName);
					}
				}
			}
		}

		return null;
	}

	/**
	 * Gets all components defined in the given InfoObjects as generated Component
	 * objects. If a component is defined twice or more times, it is still returned
	 * once.
	 * 
	 * @param infoObjects      defining CollectedMavenInfoObjects.
	 * @param withDependencies if true, generates and fills the Dependencies Object
	 *                         for each of the services.
	 * @return List of found Components.
	 */
	public List<Component> getComponents(List<CollectedMavenInfoObject> infoObjects, boolean withDependencies) {
		Set<Component> components = new HashSet<>();

		for (CollectedMavenInfoObject infoObject : infoObjects) {
			for (ModuleToComponentInfoObject componentsInfo : infoObject.getComponents()) {
				for (ComponentInfoObject pkgInfo : componentsInfo.getComponents()) {
					Component component = new Component();
					component.setName(pkgInfo.getPackageName());

					if (withDependencies) {
						Dependencies deps = new Dependencies();
						deps.getDependency().addAll(pkgInfo.getDependsOn());
						component.setComponentDependencies(deps);
					}

					components.add(component);
				}
			}
		}
		return Lists.newArrayList(components);
	}

	/**
	 * Adds the dependencies between services to the Service objects.
	 * 
	 * @param infoObjects defining CollectedMavenInfoObject.
	 * @param components  services to add dependencies to.
	 * @return the systems with the dependencies added.
	 */
	public List<Component> addComponentDependencies(List<CollectedMavenInfoObject> infoObjects,
			List<Component> components) {
		for (CollectedMavenInfoObject infoObject : infoObjects) {
			for (ModuleToComponentInfoObject componentsInfo : infoObject.getComponents()) {
				for (ComponentInfoObject pkgInfo : componentsInfo.getComponents()) {
					if (pkgInfo.getDependsOn() != null && !pkgInfo.getDependsOn().isEmpty()) {
						Component currentComponent = getComponent(components, pkgInfo.getPackageName());
						Dependencies deps = new Dependencies();
						deps.getDependency().addAll(pkgInfo.getDependsOn());
						currentComponent.setComponentDependencies(deps);
					}
				}
			}
		}

		return components;
	}

	public List<Component> addComponentRestDependencies(List<CollectedMavenInfoObject> infoObjects,
			List<Dependency> serviceDependencies, List<Component> components, boolean asRestDependency) {
		for (Component component : components) {
			for (Dependency dependency : serviceDependencies) {
				if (dependency.getServicePackage().equals(component.getName())) {
					if (component.getComponentDependencies() == null) {
						Dependencies dependencies = new Dependencies();
						component.setComponentDependencies(dependencies);
					}
					if (asRestDependency) {
					RestDependency restDependency = new RestDependency();
					restDependency.setCalls(dependency.getDependsOnPackage());
					restDependency.setMethod(dependency.getMethod());
					restDependency.setPath(dependency.getPath());

					component.getComponentDependencies().getRestDependency().add(restDependency);
					} else {
						if (!component.getComponentDependencies().getDependency().contains(dependency.getDependsOnPackage())) {
							component.getComponentDependencies().getDependency().add(dependency.getDependsOnPackage());
						}
					}
				}
			}
		}

		return components;
	}

	/**
	 * Searches in the list of Module objects for the module of the given name. If
	 * there is no such Module, a new Module is generated and added to the list.
	 * 
	 * @param modules    List to look through for the module.
	 * @param moduleName Name of the searched module.
	 * @return Found or created module.
	 */
	private Module getModule(List<Module> modules, String moduleName) {
		for (Module module : modules) {
			if (module.getName().equals(moduleName)) {
				return module;
			}
		}
		Module mod = new Module(); // TODO: return null?
		mod.setName(moduleName);
		modules.add(mod);
		return mod;
	}

	/**
	 * Searches in the list of Component objects for the component of the given
	 * name. If there is no such Component, a new Component is generated and added
	 * to the list.
	 * 
	 * @param components    List to look through for the component.
	 * @param ComponentName Name of the searched component.
	 * @return Found or created component.
	 */
	private Component getComponent(List<Component> components, String componentName) {
		for (Component component : components) {
			if (component.getName().equals(componentName)) {
				return component;
			}
		}
		Component component = new Component();
		// TODO: return null?
		component.setName(componentName);
		components.add(component);
		return component;
	}

	private String getModuleNameFromTag(CollectedMavenInfoObject infoObject, String tag) {

		for (ModuleInfoObject moduleInfoObject : infoObject.getModules()) {
			if (moduleInfoObject.getTag().equalsIgnoreCase(tag)) {
				return moduleInfoObject.getModuleName();
			}
		}

		return tag;
	}

	private Module getModuleFromNames(String name, List<Module> modules) {
		for (Module module : modules) {
			if (module.getName().equals(name)) {
				return module;
			}
		}

		return null;
	}

	private Map<String, String> getComponentToModule(List<CollectedMavenInfoObject> infoObjects) {
		Map<String, String> componentToModule = new HashMap<>();

		for (CollectedMavenInfoObject info : infoObjects) {
			for (ModuleToComponentInfoObject mTC : info.getComponents()) {
				String moduleName = mTC.getModuleName();
				for (ComponentInfoObject comp : mTC.getComponents()) {
					componentToModule.put(comp.getPackageName(), moduleName);
				}
			}
		}

		return componentToModule;
	}
}
