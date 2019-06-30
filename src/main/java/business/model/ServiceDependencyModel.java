package business.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ServiceDependencyModel {
	
	private String dependantServiceName;
	private List<String> methods;
	
	public String getDependantServiceName() {
		return dependantServiceName;
	}
	
	public void setDependantServiceName(String dependantServiceName) {
		this.dependantServiceName = dependantServiceName;
	}

	public List<String> getMethods() {
		return methods;
	}

	public void setMethods(List<String> methods) {
		this.methods = methods;
	}
	
	public void addMethod(String method) {
		if (this.methods == null) {
			this.methods = new ArrayList<>();
		}
		methods.add(method);
	}
	
	public void addMethod(Collection<String> method) {
		if (this.methods == null) {
			this.methods = new ArrayList<>();
		}
		methods.addAll(method);
	}

}
