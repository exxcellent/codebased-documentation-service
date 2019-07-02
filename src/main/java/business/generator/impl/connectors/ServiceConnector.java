package business.generator.impl.connectors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import annotation.ConsumesAPI;
import business.model.Dependency;
import collectors.models.restapi.APIInfoObject;
import collectors.models.restapi.CollectedAPIInfoObject;
import util.ConsumeDescriptionTriple;

public class ServiceConnector {

	public List<Dependency> connectServices(List<CollectedAPIInfoObject> apiInfos) {
		List<Dependency> serviceDependencyDescription = new ArrayList<>();
		if (apiInfos == null || apiInfos.isEmpty()) {
			return serviceDependencyDescription;
		}

		Map<String, List<ConsumeDescriptionTriple>> leftovers = null;

		List<String> serviceNames = new ArrayList<>();
		for (CollectedAPIInfoObject currentInfo : apiInfos) {
			serviceNames.add(currentInfo.getServiceName());
		}

		Map<String, List<ConsumeDescriptionTriple>> consumeTriples = new HashMap<>();
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
			providesTriples.add(apiInfo.getProvide());
		}
		serviceNames.add("external");

		leftovers = matchByServiceName(consumeTriples, providesTriples, serviceDependencyDescription, serviceNames);
		leftovers = matchByPath(leftovers, providesTriples, serviceDependencyDescription);
		setLeftoversExternal(leftovers, serviceDependencyDescription);
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
	private Map<String, List<ConsumeDescriptionTriple>> matchByServiceName(
			Map<String, List<ConsumeDescriptionTriple>> consumeTriples, List<APIInfoObject> providesTriples,
			List<Dependency> serviceDependencyDescription, List<String> serviceNames) {

		Map<String, List<ConsumeDescriptionTriple>> consumeWithoutMatchOrName = new HashMap<>();

		for (Entry<String, List<ConsumeDescriptionTriple>> currentTripleEntry : consumeTriples.entrySet()) {

			String currentService = currentTripleEntry.getKey();
			
			for (ConsumeDescriptionTriple currentTriple : currentTripleEntry.getValue()) {
				String dependServiceName = currentTriple.getServiceName();
				if (serviceNames.contains(dependServiceName) && !dependServiceName.equals(ConsumesAPI.DEFAULT_SERVICE)) {
					APIInfoObject matchingService = getApiInfoObjectByServiceName(providesTriples, dependServiceName);
					List<Dependency> matches = getMatchingPathAndMethod(currentTripleEntry.getValue(), matchingService);
					if (matches != null) {
						serviceDependencyDescription.addAll(matches);
					} else {
						System.out.println("No matching path and method in " + currentService + " on " + dependServiceName);
					}
				} else {
					if (consumeWithoutMatchOrName.containsKey(currentService)) {
						consumeWithoutMatchOrName.get(currentService).add(currentTriple);
					} else {
						consumeWithoutMatchOrName.put(currentService, Lists.newArrayList(currentTriple));
					}
				}
			}

		}

		return consumeWithoutMatchOrName;
	}

	private Map<String, List<ConsumeDescriptionTriple>> matchByPath(
			Map<String, List<ConsumeDescriptionTriple>> consumeTriples, List<APIInfoObject> providesTriples,
			List<Dependency> serviceDependencyDescription) {

		Map<String, List<ConsumeDescriptionTriple>> consumeWithoutMatch = new HashMap<>();
		Map<String, Set<String>> pathToServices = getServiceNameToPathMap(providesTriples);

		for (Entry<String, List<ConsumeDescriptionTriple>> entry : consumeTriples.entrySet()) {
			String currentService = entry.getKey();
			for (ConsumeDescriptionTriple triple : entry.getValue()) {
				Set<String> depServices = pathToServices.get(formatPath(triple.getPath()));

				if (depServices != null && !depServices.isEmpty()) {
					for (String serv : depServices) {
						for (String method : triple.getMethods()) {							
							
							Dependency dependency = new Dependency();
							dependency.setDependsOn(serv);
							dependency.setService(currentService);
							dependency.setPath(triple.getPath());
							dependency.setMethod(method);							

							if (depServices.size() > 1) {
								dependency.setAmbiguous(true);
							} else {
								dependency.setAmbiguous(false);
							}
							System.out.println(
									"created dependency: " + dependency.getService() + " -> " + dependency.getDependsOn());
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

		return consumeWithoutMatch;
	}

	private void setLeftoversExternal(Map<String, List<ConsumeDescriptionTriple>> leftovers,
			List<Dependency> serviceDependencyDescription) {
		for (Entry<String, List<ConsumeDescriptionTriple>> entry : leftovers.entrySet()) {
			for (ConsumeDescriptionTriple triple : entry.getValue()) {
				for (String meth : triple.getMethods()) {
					Dependency dependency = new Dependency();
					dependency.setService(entry.getKey());
					dependency.setDependsOn("external");
					dependency.setPath(triple.getPath());
					dependency.setMethod(meth);
					dependency.setAmbiguous(true);

					serviceDependencyDescription.add(dependency);
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

	private List<Dependency> getMatchingPathAndMethod(List<ConsumeDescriptionTriple> triples,
			APIInfoObject matchingServiceObject) {

		if (matchingServiceObject == null) {
			return null;
		}
		List<Dependency> dependencies = new ArrayList<>();

		for (ConsumeDescriptionTriple currentTriple : triples) {
			List<Dependency> firstMatches = null;
			boolean othersFound = false;

			for (String comparePath : matchingServiceObject.getPathToMethod().keySet()) {

				if (isMatchingPath(currentTriple.getPath(), comparePath)) {
					Set<String> compareMethods = matchingServiceObject.getPathToMethod().get(comparePath);
					List<Dependency> methodAndPathMatches = new ArrayList<>();
					for (String currentMethod : currentTriple.getMethods()) {
						if (compareMethods.contains(currentMethod)) {
							Dependency dependency = new Dependency();
							dependency.setService(currentTriple.getServiceName());
							dependency.setDependsOn(matchingServiceObject.getMicroserviceName());
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
							System.out.println("Method " + currentMethod + " did not match. Could be: ");
							for (String pM : currentTriple.getMethods()) {
								System.out.println(" - " + pM);
							}
						}
					}
					if (!methodAndPathMatches.isEmpty() && firstMatches == null) {
						firstMatches = methodAndPathMatches;
					} else {
						dependencies.addAll(methodAndPathMatches);
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
			for (String path : apiInfo.getPathToMethod().keySet()) {
				String formattedPath = formatPath(path);
				if (pathToServices.containsKey(formattedPath)) {
					pathToServices.get(formattedPath).add(apiInfo.getMicroserviceName());
				} else {
					pathToServices.put(formattedPath, Sets.newHashSet(apiInfo.getMicroserviceName()));
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

	private String formatPath(String path) {
		return path.replaceAll("\\{.*\\}", "{}"); // TODO: TYPE CHECK! replace with type instead of empty brackets
	}

}
