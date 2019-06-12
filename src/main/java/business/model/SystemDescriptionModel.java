package business.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemDescriptionModel {
	
	private String systemName;
	private Map<String, List<String>> subsysToMS;
	
	public SystemDescriptionModel(String system) {
		this.systemName = system;
		this.subsysToMS = new HashMap<>();
	}
	
	public String getSystemName() {
		return systemName;
	}
	
	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}
	
	public Map<String, List<String>> getSubsysToMS() {
		return subsysToMS;
	}
	
	public void setSubsysToMS(Map<String, List<String>> subsysToMS) {
		this.subsysToMS = subsysToMS;
	}
	
	public void addSubsystem(String subsystem) {
		if (this.subsysToMS.containsKey(subsystem)) {
			return;
		}
		this.subsysToMS.put(subsystem, new ArrayList<>());
	}
	
	public void addSubsystem(String subsystem, String microservice) {
		if (this.subsysToMS.containsKey(subsystem)) {
			this.subsysToMS.get(subsystem).add(microservice);
			return;
		}
		List<String> ms = new ArrayList<String>();
		ms.add(microservice);
		this.subsysToMS.put(subsystem, ms);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SystemDescriptionModel) {
			return this.systemName.equals(((SystemDescriptionModel) obj).getSystemName());
		}
		return false;
	}

}
