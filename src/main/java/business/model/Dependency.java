package business.model;

public class Dependency {

	private String service;
	private String dependsOn;
	private String servicePackage;
	private String dependsOnPackage;
	private String path;
	private String method;
	private boolean ambiguous;

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getDependsOn() {
		return dependsOn;
	}

	public void setDependsOn(String dependsOn) {
		this.dependsOn = dependsOn;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public boolean isAmbiguous() {
		return ambiguous;
	}

	public void setAmbiguous(boolean ambiguous) {
		this.ambiguous = ambiguous;
	}

	public boolean mergeDependency(Dependency dependency) {
		if (this.service.equalsIgnoreCase(dependency.getService())
				&& this.method.equalsIgnoreCase(dependency.getDependsOn())
				&& this.getDependsOn().equalsIgnoreCase(dependency.getDependsOn())) {
			
			if (this.method == null || this.method.isEmpty()) {
				this.method = dependency.getMethod();
			} else {
				this.method += ", " + dependency.getMethod();
			}
			
			return true;
		}
		return false;
	}

	public String getServicePackage() {
		return servicePackage;
	}

	public void setServicePackage(String servicePackage) {
		this.servicePackage = servicePackage;
	}

	public String getDependsOnPackage() {
		return dependsOnPackage;
	}

	public void setDependsOnPackage(String dependsOnPackage) {
		this.dependsOnPackage = dependsOnPackage;
	}

}
