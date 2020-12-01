package com.terminalstuff;

import java.util.HashMap;
import java.util.Map;

public class CommandQueue {

	@SuppressWarnings("rawtypes")
	private Map<String, Map> servers = new HashMap<String, Map>();
	
	public CommandQueue() {
		
	}
	
	@SuppressWarnings("unchecked")
	public void addCommand(String plrName, String jobId, String command) {
		if(servers.get(jobId) == null) {
			servers.put(jobId, new HashMap<String, String>());
		}
		servers.get(jobId).put(plrName, command);
		
	};
	
	@SuppressWarnings("unchecked")
	public Map<String, String> getArr(String jobId) {
		if(servers.get(jobId) == null) {
			servers.put(jobId, new HashMap<String, String>());
		}
		return servers.get(jobId);
	}
	
	public void clearServerCommands( String jobId ) {
		if(servers.get(jobId) == null) {
			servers.put(jobId, new HashMap<String, String>());
		}
		servers.get(jobId).clear();
	}
}
