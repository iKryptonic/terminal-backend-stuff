package com.terminalstuff;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BanHandler {
	public BanHandler() {
		
	}// end default constructor
	
	@SuppressWarnings("unchecked")
	public String getBanByName(String place, String name) throws JSONException {
		String rtn = "";
		Iterator<String> allbans = null;
		switch(place) {
		case "1":
			if(MainClass.place1bans!=null) {
				allbans = MainClass.place1bans.keys();
			}
			break;
		case "2":
			if(MainClass.place2bans!=null) {
				allbans = MainClass.place2bans.keys();
			}
			break;
		}
		if(allbans!=null) {
			while(allbans.hasNext()) {
				String userId = allbans.next();
				JSONObject currentTable = null;
				if(place.equals("1")){
					currentTable = (JSONObject)MainClass.place1bans.get(userId);
				} else if(place.equals("2")) {
					currentTable = (JSONObject)MainClass.place2bans.get(userId);
				}
				String userName = currentTable.getString("Name");
				String bannedBy = currentTable.getString("BannedBy");
				String timeStamp = Integer.toString(currentTable.getInt("Timestamp"));
				String reason = currentTable.getString("Reason");
				String duration = Double.toString(currentTable.getDouble("Duration"));
				
				if(userName.equals(name)) {
					
					Date date = new Date(Long.parseLong(timeStamp)*1000L); 
					SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss z"); 
					String formattedDate = sdf.format(date);
					
					StringBuilder toReturn = new StringBuilder();
					toReturn.append("UserName: "+userName+"\n");
					toReturn.append("Moderator: "+bannedBy+"\n");
					toReturn.append("Banned on: "+formattedDate+"\n");
					toReturn.append("Original Duration: "+duration+"\n");
					toReturn.append("Reason: "+reason);
					rtn = toReturn.toString();
					break;
				}
			}
		} else { 
			rtn = "yeah... that place dont exist fam.";
		}
		return rtn;
	}
	
	@SuppressWarnings("unchecked")
	public String[] getBanCountByMod(String name) throws JSONException {
		String[] rtn = new String[2];
		
		rtn[0] = "bad request";
		rtn[1] = "go run terminal on both places ya dingus";
		
		Iterator<String> allbans1 = MainClass.place1bans.keys();
		Iterator<String> allbans2 = MainClass.place2bans.keys();
		
		int place1bans = 0;
		int place2bans = 0;
		
		if(allbans1!=null && allbans2!=null) {
			while(allbans2.hasNext()) {
				String userId = allbans2.next();
				JSONObject currentTable = (JSONObject)MainClass.place2bans.get(userId);
				String bannedBy = currentTable.getString("BannedBy");
				
				if(bannedBy.equals(name)) {
					place2bans++;
				}
			}
			while(allbans1.hasNext()) {
				String userId = allbans1.next();
				JSONObject currentTable = (JSONObject)MainClass.place1bans.get(userId);
				String bannedBy = currentTable.getString("BannedBy");
				
				if(bannedBy.equals(name)) {
					place1bans++;
				}
			}
			if(place1bans==0 && place2bans==0) {
				rtn[0] = "bad request";
				rtn[1] = "this isnt even a mod or they dont moderate enough! stop being dingus!";
			} else {
				rtn[0] = Integer.toString(place1bans);
				rtn[1] = Integer.toString(place2bans);
			}
		} else { 
			rtn[0] = "bad request";
			rtn[1] = "go run terminal on both places ya dingus";
		}
		return rtn;
	}
}
