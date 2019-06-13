package business.generator.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import collectors.models.maven.CollectedMavenInfoObject;
import collectors.models.maven.ComponentInfoObject;
import collectors.models.maven.ModuleInfoObject;
import collectors.models.maven.PackageInfoObject;
import data.model.xml.Component;
import data.model.xml.Dependencies;
import data.model.xml.Module;
import data.model.xml.Service;
import data.model.xml.Subsystem;
import data.model.xml.System;

public class XMLObjectCreator {

	public List<System> getSystems(List<CollectedMavenInfoObject> infoObjects) {
		Set<System> systemList = new HashSet<>();
		for (CollectedMavenInfoObject infoObject : infoObjects) {
			System sys = new System();
			sys.setName(infoObject.getSystem());
			systemList.add(sys);
		}
		return Lists.newArrayList(systemList);
	}

	public List<System> addSystemDependencies(List<CollectedMavenInfoObject> infoObjects, List<System> systems) {
		// TODO: implement when infoObject contains this info.
		return systems;
	}

	public List<System> addSubsystems(List<CollectedMavenInfoObject> infoObjects, List<System> systems) {
		for (System currentSystem : systems) {
			Set<Subsystem> subsystems = new HashSet<>();
			for (CollectedMavenInfoObject infoObject : infoObjects) {
				if (infoObject.getSystem().equals(currentSystem.getName())) {
					Subsystem sub = new Subsystem();
					sub.setName(infoObject.getSubsystem());
					subsystems.add(sub);
				}
			}
			currentSystem.getSubsystems().addAll(subsystems);
		}
		return systems;
	}

	public List<Subsystem> getSubsystems(List<CollectedMavenInfoObject> infoObjects) {
		Set<Subsystem> subsystems = new HashSet<>();

		for (CollectedMavenInfoObject infoObject : infoObjects) {
			Subsystem subsystem = new Subsystem();
			subsystem.setName(infoObject.getSubsystem());
			subsystems.add(subsystem);
		}

		return Lists.newArrayList(subsystems);
	}

	public List<Subsystem> addSubystemDependencies(List<CollectedMavenInfoObject> infoObjects,
			List<Subsystem> subsystems) {
		// TODO: implement when infoObject contains this info.
		return subsystems;
	}

	public List<Subsystem> addServices(List<CollectedMavenInfoObject> infoObjects, List<Subsystem> subsystems) {
		for (Subsystem subsystem : subsystems) {
			Set<Service> services = new HashSet<>();
			for (CollectedMavenInfoObject infoObject : infoObjects) {
				if (infoObject.getSubsystem().equals(subsystem.getName())) {
					Service service = new Service();
					service.setName(infoObject.getProjectName());
					services.add(service);
				}
			}
			subsystem.getServices().addAll(services);
		}
		return subsystems;
	}

	public List<Service> getServices(List<CollectedMavenInfoObject> infoObjects, boolean withDependencies) {
		Set<Service> services = new HashSet<>();

		for (CollectedMavenInfoObject infoObject : infoObjects) {
			Service service = new Service();
			service.setName(infoObject.getProjectName());

			if (withDependencies) {
				// TODO: set dependencies.
			}

			services.add(service);
		}

		return Lists.newArrayList(services);
	}

	public List<Service> addServiceDependencies(List<CollectedMavenInfoObject> infoObjects, List<Service> services) {
		// TODO: implement when infoObject contains this info.
		return services;
	}

	public List<Service> addModules(List<CollectedMavenInfoObject> infoObjects, List<Service> services) {
		for (Service service : services) {
			Set<Module> modules = new HashSet<>();
			for (CollectedMavenInfoObject infoObject : infoObjects) {
				if (infoObject.getProjectName().equals(service.getName())) {
					for (ModuleInfoObject moduleInfo : infoObject.getModules()) {
						Module module = new Module();
						module.setName(moduleInfo.getModuleName());
						modules.add(module);
					}
				}
			}
			service.getModules().addAll(modules);
		}
		return services;
	}

	public List<Service> addModulesWithDependencies(List<CollectedMavenInfoObject> infoObjects,
			List<Service> services) {
		for (Service service : services) {
			Set<Module> modules = new HashSet<>();
			for (CollectedMavenInfoObject infoObject : infoObjects) {
				if (infoObject.getProjectName().equals(service.getName())) {
					for (ModuleInfoObject moduleInfo : infoObject.getModules()) {
						Module module = new Module();
						module.setName(moduleInfo.getModuleName());

						Dependencies deps = new Dependencies();
						deps.getDependency().addAll(moduleInfo.getDependsOn());

						modules.add(module);
					}
				}
			}
			service.getModules().addAll(modules);
		}
		return services;
	}

	public List<Module> getModules(List<CollectedMavenInfoObject> infoObjects, boolean withDependencies) {
		Set<Module> modules = new HashSet<>();

		for (CollectedMavenInfoObject infoObject : infoObjects) {
			for (ModuleInfoObject moduleInfo : infoObject.getModules()) {
				Module module = new Module();
				module.setName(moduleInfo.getModuleName());

				if (withDependencies) {
					Dependencies deps = new Dependencies();
					deps.getDependency().addAll(moduleInfo.getDependsOn());
				}

				modules.add(module);
			}
		}

		return Lists.newArrayList(modules);
	}

	public List<Module> addModuleDependencies(List<CollectedMavenInfoObject> infoObjects, List<Module> modules) {

		for (CollectedMavenInfoObject infoObject : infoObjects) {
			for (ModuleInfoObject moduleInfo : infoObject.getModules()) {
				Module currentModule = getModule(modules, moduleInfo.getModuleName());
				if (moduleInfo.getDependsOn() != null && !moduleInfo.getDependsOn().isEmpty()) {
					Dependencies dep = new Dependencies();
					dep.getDependency().addAll(moduleInfo.getDependsOn());
					currentModule.setModuleDependencies(dep);
				}
			}
		}

		return modules;
	}

	private Module getModule(List<Module> modules, String moduleName) {
		for (Module module : modules) {
			if (module.getName().equals(moduleName)) {
				return module;
			}
		}
		Module mod = new Module(); // TODO: return null?
		mod.setName(moduleName);
		return mod;
	}

	public List<Module> addComponents(List<CollectedMavenInfoObject> infoObjects, List<Module> modules) {
		for (CollectedMavenInfoObject infoObject : infoObjects) {
			for (ComponentInfoObject componentsInfo : infoObject.getComponents()) {
				Module currentMod = getModule(modules, componentsInfo.getModuleName());

				for (PackageInfoObject pkgInfo : componentsInfo.getComponents()) {
					Component component = new Component();
					component.setName(pkgInfo.getPackageName());
					currentMod.getComponents().add(component);
				}
			}
		}

		return modules;
	}

	public List<Module> addComponentsWithDependencies(List<CollectedMavenInfoObject> infoObjects,
			List<Module> modules) {
		for (CollectedMavenInfoObject infoObject : infoObjects) {
			for (ComponentInfoObject componentsInfo : infoObject.getComponents()) {
				Module currentMod = getModule(modules, componentsInfo.getModuleName());

				for (PackageInfoObject pkgInfo : componentsInfo.getComponents()) {
					Component component = new Component();
					component.setName(pkgInfo.getPackageName());

					Dependencies deps = new Dependencies();
					deps.getDependency().addAll(pkgInfo.getDependsOn());
					component.setComponentDependencies(deps);

					currentMod.getComponents().add(component);
				}
			}
		}

		return modules;
	}

	public List<Component> getComponents(List<CollectedMavenInfoObject> infoObjects, boolean withDependencies) {
		Set<Component> components = new HashSet<>();

		for (CollectedMavenInfoObject infoObject : infoObjects) {
			for (ComponentInfoObject componentsInfo : infoObject.getComponents()) {
				for (PackageInfoObject pkgInfo : componentsInfo.getComponents()) {
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

}
