package business.generator.impl.connectors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import annotation.ConsumesAPI;
import business.model.Dependency;
import collectors.models.maven.CollectedMavenInfoObject;
import collectors.models.maven.ComponentInfoObject;
import collectors.models.maven.ModuleToComponentInfoObject;
import collectors.models.restapi.APIInfoObject;
import collectors.models.restapi.CollectedAPIInfoObject;
import util.ConsumeDescription;
import util.HttpMethods;
import util.OfferDescription;

public class ServiceConnector {

	public List<Dependency> connectServices(List<CollectedAPIInfoObject> apiInfos,
			List<CollectedMavenInfoObject> mavenInfos) {
		List<Dependency> serviceDependencyDescription = new ArrayList<>();
		if (apiInfos == null || apiInfos.isEmpty()) {
			return serviceDependencyDescription;
		}

		Map<String, List<ConsumeDescription>> leftovers = null;

		List<String> serviceNames = new ArrayList<>();
		for (CollectedAPIInfoObject currentInfo : apiInfos) {
			serviceNames.add(currentInfo.getServiceName());
		}

		Map<String, List<ConsumeDescription>> consumeTriples = new HashMap<>();
		List<APIInfoObject> providesTriples = new ArrayList<>();

		for (CollectedAPIInfoObject apiInfo : apiInfos) {
			if (apiInfo == null || apiInfo.getConsume() == null) {
				break;
			}
			if (apiInfo.getServiceName() != null && !apiInfo.getServiceName().equals(ConsumesAPI.DEFAULT_SERVICE)) {
				consumeTriples.put(apiInfo.getServiceName(), apiInfo.getConsume());
			} else if (consumeTriples.containsKey(ConsumesAPI.DEFAULT_SERVICE)
					&& consumeTriples.get(ConsumesAPI.DEFAULT_SERVICE) != null) {
				consumeTriples.get(ConsumesAPI.DEFAULT_SERVICE).addAll(apiInfo.getConsume());
			} else {
				consumeTriples.put(ConsumesAPI.DEFAULT_SERVICE, apiInfo.getConsume());
			}

			if (apiInfo.getProvide().getApi() != null) {
				providesTriples.add(apiInfo.getProvide());
			}

		}
		serviceNames.add("external");

		leftovers = matchByServiceName(consumeTriples, providesTriples, serviceDependencyDescription, serviceNames);
		leftovers = matchByPath(leftovers, providesTriples, serviceDependencyDescription);
		setLeftoversExternal(leftovers, serviceDependencyDescription);
		
		if (mavenInfos != null) {
			serviceDependencyDescription = refineDependencies(serviceDependencyDescription, mavenInfos);
		}
		return serviceDependencyDescription;
	}

	/**
	 * Find dependencies by looking at the service names in the consume triples of
	 * the given apiInfos. If there is a name given in the triple, but no fitting
	 * counterpart in the apiInfo list, or the default service name is given, the
	 * triple is added to the returned list.
	 * 
	 * @param apiInfos
	 * @param serviceDependencies
	 * @return
	 */
	private Map<String, List<ConsumeDescription>> matchByServiceName(
			Map<String, List<ConsumeDescription>> serviceNameToConsumeTriples, List<APIInfoObject> providesTriples,
			List<Dependency> serviceDependencyDescription, List<String> serviceNames) {

		Map<String, List<ConsumeDescription>> consumeWithoutMatchOrName = new HashMap<>();

		for (Entry<String, List<ConsumeDescription>> currentServiceToTripleEntry : serviceNameToConsumeTriples
				.entrySet()) {

			String currentService = currentServiceToTripleEntry.getKey();

			for (ConsumeDescription currentConsumeDescription : currentServiceToTripleEntry.getValue()) {
				String dependServiceName = currentConsumeDescription.getServiceName();

				if (serviceNames.contains(dependServiceName)
						&& !dependServiceName.equals(ConsumesAPI.DEFAULT_SERVICE)) {
					APIInfoObject matchingService = getApiInfoObjectByServiceName(providesTriples, dependServiceName);

					List<Dependency> matches = getMatchingPathAndMethod(currentServiceToTripleEntry.getValue(),
							matchingService);
					if (matches != null) {
						serviceDependencyDescription.addAll(matches);
					} else {
						System.out.println(
								"No matching path and method in " + currentService + " on " + dependServiceName);
					}
				} else {
					if (consumeWithoutMatchOrName.containsKey(currentService)) {
						consumeWithoutMatchOrName.get(currentService).add(currentConsumeDescription);
					} else {
						consumeWithoutMatchOrName.put(currentService, Lists.newArrayList(currentConsumeDescription));
					}
				}
			}

		}

		return consumeWithoutMatchOrName;
	}

	private String getPackageOfPath(List<APIInfoObject> apiInfos, String service, String path) {
		for (APIInfoObject info : apiInfos) {
			if (info.getMicroserviceName().equalsIgnoreCase(service)) {
				for (OfferDescription desc : info.getApi()) {
					if (desc.getPathToMethodMappings().containsKey(path)) {
						return desc.getPackageName();
					}
				}
			}
		}
		return "";
	}

	private Map<String, List<ConsumeDescription>> matchByPath(Map<String, List<ConsumeDescription>> consumeTriples,
			List<APIInfoObject> providesTriples, List<Dependency> serviceDependencyDescription) {

		Map<String, List<ConsumeDescription>> consumeWithoutMatch = new HashMap<>();
		Map<String, Set<String>> pathToServices = getServiceNameToPathMap(providesTriples);

		for (Entry<String, List<ConsumeDescription>> entry : consumeTriples.entrySet()) {
			String currentService = entry.getKey();
			for (ConsumeDescription triple : entry.getValue()) {

				for (String consumePath : triple.getPathToMethods().keySet()) {
					Set<String> depServices = pathToServices.get(formatPath(consumePath));

					if (depServices != null && !depServices.isEmpty()) {
						for (String serv : depServices) {
							for (String method : getMethodsOfConsumes(triple, consumePath)) {

								Dependency dependency = new Dependency();
								dependency.setService(currentService);
								dependency.setDependsOn(serv);
								dependency.setServicePackage(triple.getPackageName());
								dependency.setDependsOnPackage(getPackageOfPath(providesTriples, serv, consumePath));
								dependency.setPath(consumePath);
								dependency.setMethod(method);

								if (depServices.size() > 1) {
									dependency.setAmbiguous(true);
								} else {
									dependency.setAmbiguous(false);
								}
								serviceDependencyDescription.add(dependency);
							}

						}
					} else {

						if (consumeWithoutMatch.containsKey(currentService)) {
							consumeWithoutMatch.get(currentService).add(triple);
						} else {
							consumeWithoutMatch.put(currentService, Lists.newArrayList(triple));
						}
					}
				}

			}

		}
		return consumeWithoutMatch;
	}

	private void setLeftoversExternal(Map<String, List<ConsumeDescription>> leftovers,
			List<Dependency> serviceDependencyDescription) {
		for (Entry<String, List<ConsumeDescription>> entry : leftovers.entrySet()) {
			for (ConsumeDescription triple : entry.getValue()) {
				for (Entry<String, Set<String>> pathToMethods : triple.getPathToMethods().entrySet()) {

					for (String method : pathToMethods.getValue()) {
						Dependency dependency = new Dependency();
						dependency.setService(entry.getKey());
						dependency.setDependsOn("external");
						dependency.setPath(pathToMethods.getKey());
						dependency.setMethod(method);
						dependency.setAmbiguous(true);

						serviceDependencyDescription.add(dependency);
					}
				}
			}
		}
	}

	private APIInfoObject getApiInfoObjectByServiceName(List<APIInfoObject> apiInfos, String serviceName) {
		for (APIInfoObject apiInfo : apiInfos) {
			if (apiInfo.getMicroserviceName().equals(serviceName)) {
				return apiInfo;
			}
		}
		return null;
	}

	private Set<String> getMethodsOfConsumes(ConsumeDescription consumes, String path) {
		if (consumes.getPathToMethods().containsKey(path)) {
			return consumes.getPathToMethods().get(path);

		}
		return null;
	}

	private Set<String> getMethodsOfOffers(List<OfferDescription> offers, String path) {
		for (OfferDescription description : offers) {
			if (description.getPathToMethodMappings().containsKey(path)) {
				Set<String> returnMethods = new HashSet<>();
				for (HttpMethods meth : description.getPathToMethodMappings().get(path)) {
					returnMethods.add(meth.toString());
				}
				return returnMethods;
			}
		}
		return null;
	}

	private List<Dependency> getMatchingPathAndMethod(List<ConsumeDescription> triples,
			APIInfoObject matchingServiceObject) {

		if (matchingServiceObject == null) {
			return null;
		}
		List<Dependency> dependencies = new ArrayList<>();

		for (ConsumeDescription currentTriple : triples) {
			List<Dependency> firstMatches = null;
			boolean othersFound = false;
			
			for (OfferDescription description : matchingServiceObject.getApi()) {
				for (String comparePath : description.getPathToMethodMappings().keySet()) {

					for (String pathOfCurrentTriple : currentTriple.getPathToMethods().keySet()) {

						if (isMatchingPathAndParameterType(pathOfCurrentTriple, comparePath)) {
							Set<String> compareMethods = getMethodsOfOffers(matchingServiceObject.getApi(),
									comparePath);
							List<Dependency> methodAndPathMatches = new ArrayList<>();

							for (String currentMethod : currentTriple.getPathToMethods().get(pathOfCurrentTriple)) {
								if (compareMethods.contains(currentMethod)) {
									Dependency dependency = new Dependency();
									dependency.setService(currentTriple.getServiceName());
									dependency.setDependsOn(matchingServiceObject.getMicroserviceName());
									dependency.setServicePackage(currentTriple.getPackageName());
									dependency.setDependsOnPackage(description.getPackageName());
									dependency.setMethod(currentMethod);
									dependency.setPath(comparePath);
									if (firstMatches == null) {
										dependency.setAmbiguous(false);
									} else {
										dependency.setAmbiguous(true);
										othersFound = true;
									}
									methodAndPathMatches.add(dependency);
								} else {
									System.out.println("Method " + currentMethod + " did not match.");
								}
							}

							if (!methodAndPathMatches.isEmpty() && firstMatches == null) {
								firstMatches = methodAndPathMatches;
							} else {
								dependencies.addAll(methodAndPathMatches);
							}
						}
					}

				}

			}

			if (firstMatches != null && othersFound) {
				firstMatches.forEach(dep -> dep.setAmbiguous(true));
			} else if (firstMatches != null) {
				dependencies.addAll(firstMatches);
			}
		}

		return dependencies;

	}

	private Map<String, Set<String>> getServiceNameToPathMap(List<APIInfoObject> apiInfos) {
		Map<String, Set<String>> pathToServices = new HashMap<>();

		if (apiInfos == null || apiInfos.isEmpty()) {
			return pathToServices;
		}

		for (APIInfoObject apiInfo : apiInfos) {
			for (OfferDescription offer : apiInfo.getApi()) {
				for (String path : offer.getPathToMethodMappings().keySet()) {
					String formattedPath = formatPath(path);
					if (pathToServices.containsKey(formattedPath)) {
						pathToServices.get(formattedPath).add(apiInfo.getMicroserviceName());
					} else {
						pathToServices.put(formattedPath, Sets.newHashSet(apiInfo.getMicroserviceName()));
					}
				}
			}
		}

		return pathToServices;
	}

	private boolean isMatchingPath(String path1, String path2) {
		path1 = formatPath(path1);
		path2 = formatPath(path2);

		if (path1.equalsIgnoreCase(path2)) {
			return true;
		}
		return false;
	}

	private boolean isMatchingPathAndParameterType(String path1, String path2) {
		path1 = insertAnyForUnknownType(path1);
		path2 = insertAnyForUnknownType(path2);

		if (path1.equalsIgnoreCase(path2)) {
			return true;
		}

		String[] splitPath1 = path1.split("/");
		String[] splitPath2 = path2.split("/");

		if (splitPath1.length != splitPath2.length) {
			return false;
		}

		for (int i = 0; i < splitPath1.length; i++) {
			if (!splitPath1[i].equals(splitPath2[i])) {
				if (isPlaceholder(path1) && isPlaceholder(path2)) {

				}
			}
		}

		return false;
	}

	private boolean isPlaceholder(String path) {
		return path.startsWith("{") && path.endsWith("{");
	}

	private boolean matchPlaceholder(String placeholder1, String placeholder2) {
		if (placeholder1.equals("{*}") || placeholder2.equals("{*}") || placeholder1.equals(placeholder2)) {
			return true;
		} else {
			return false;
		}
	}

	private String insertAnyForUnknownType(String path) {
		String returnString = "";
		String[] splitPath = path.split("/");
		for (String pathPart : splitPath) {
			if (pathPart.startsWith("{") && pathPart.endsWith("}")) {
				switch (pathPart) {
				case "{INT}":
				case "{STRING}":
				case "{FLOAT}":
					returnString = String.join("/", pathPart);
					break;
				default:
					returnString = String.join("/", "{*}");
				}
			} else if (!pathPart.isEmpty()) {
				returnString = String.join("/", returnString, pathPart);
			}
		}
		if (returnString.isEmpty()) {
			return path;
		}
		return returnString;
	}

	private String formatPath(String path) {
		return path.replaceAll("\\{.*\\}", "{}"); // TODO: TYPE CHECK! replace with type instead of empty brackets
	}

	private List<Dependency> refineDependencies(List<Dependency> dependencies,
			List<CollectedMavenInfoObject> mavenInfos) {

		Map<String, List<String>> serviceToComponentNames = new HashMap<>();
		for (Dependency currentDependency : dependencies) {
			String service = currentDependency.getService();
			List<String> serviceComponents;
			if (serviceToComponentNames.containsKey(service)) {
				serviceComponents = serviceToComponentNames.get(service);
			} else {
				serviceComponents = getComponentNames(service, mavenInfos);
			}
			String dependsOnService = currentDependency.getDependsOn();
			List<String> dependsOnComponents;
			if (serviceToComponentNames.containsKey(dependsOnService)) {
				dependsOnComponents = serviceToComponentNames.get(dependsOnService);
			} else {
				dependsOnComponents = getComponentNames(dependsOnService, mavenInfos);
			}
			
			String component = shortenToComponent(serviceComponents, currentDependency.getServicePackage());
			String dependComponent = shortenToComponent(dependsOnComponents, currentDependency.getDependsOnPackage());
			
			currentDependency.setServicePackage(component);
			currentDependency.setDependsOnPackage(dependComponent);
		}

		return dependencies;
	}

	private List<String> getComponentNames(String serviceName, List<CollectedMavenInfoObject> mavenInfos) {
		List<ComponentInfoObject> components = new ArrayList<>();
		for (CollectedMavenInfoObject infoObject : mavenInfos) {
			if (infoObject.getProjectName().equalsIgnoreCase(serviceName)) {
				for (ModuleToComponentInfoObject mtc : infoObject.getComponents()) {
					components.addAll(mtc.getComponents());
				}
			}
		}
		List<String> names = new ArrayList<>();
		for (ComponentInfoObject comp : components) {
			names.add(comp.getPackageName());
		}
		return names;
	}

	private String shortenToComponent(List<String> componentNames, String packageName) {
		List<String> names = new ArrayList<>();
		for (String componentName : componentNames) {
			if (packageName.startsWith(componentName)) {
				names.add(componentName);
			}
		}

		if (names.size() > 1) {
			return findLongest(names);
		}
		if (!names.isEmpty()) {
			return names.get(0);			
		}
		return packageName;
	}

	private String findLongest(List<String> names) {
		String longest = "";
		for (String match : names) {
			if (match.length() > longest.length()) {
				longest = match;
			}
		}
		return longest;
	}

}
