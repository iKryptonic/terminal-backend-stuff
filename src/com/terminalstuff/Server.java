package com.terminalstuff;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
 
public class Server {
	
	String jobId;
	String serverKey;
	long lastUpdate = 0;
	boolean isDead = false;
	String place;
	Map<String, String> players = new HashMap<String, String>();
	Map<String, String> notificationlogs = new HashMap<String, String>();
	Map<Integer, String> chatlogs = new HashMap<Integer, String>();
	Map<String, String> commandlogs = new HashMap<String, String>();
	Map<Integer, Outputz> outputs = new HashMap<Integer, Outputz>();
	int numLogs = 0;
	int numOutputs = 0;
	
	public Server(String jId, String sKey, String placeId) {
		// System.out.println("New Server Created: " + sKey);
		this.isDead = false;
		this.lastUpdate = System.currentTimeMillis() / 1000L;
		this.jobId = jId;
		this.serverKey = sKey;		
		if(placeId.equals("843468296")) {
			this.place = "Place 1";
		}else if(placeId.equals("843495510")) {
			this.place = "Place 2";
		} else {
			this.place = "Unknown";
		}
		
		
		for(Entry<WebsocketClient, String> entry : MainClass.authed.entrySet()) {
			try {
				entry.getKey().outputTo("NEW SERVER CREATED: "+sKey, "notice");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void killServer(){
		// System.out.println("Server [" + serverKey + "] was killed!");
		this.isDead = true;
		this.players.clear();
	}
	
	public void updateTime() {
		this.lastUpdate = System.currentTimeMillis() / 1000L;
	}
	
	public void updatePlayers(String jsonString) {
		players.clear();
		try {
		JSONObject toDissect = new JSONObject(jsonString);
		JSONArray playersArray = toDissect.getJSONArray("player-list");
		for (int i = 0; i < playersArray.length(); i++) {
            players.put(playersArray.getString(i), serverKey);

        }
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
		
	}
	
	public void addCommand(String userName, String command) {
		commandlogs.put(command, userName);
	}
	
	public void addNotification(String userName, String msg) {
		notificationlogs.put(userName, msg);
	}
	
	public String getCommands() throws JSONException {
		JSONObject o = new JSONObject();
		o.put("commands-list", commandlogs);
		o.put("connected-clients", MainClass.authed);
		o.put("pending-notifications", notificationlogs);
        commandlogs.clear();
        notificationlogs.clear();
    return o.toString();
	}
	
	public void addOutputs(String jsonString) {
		try {
		JSONArray toDissect = new JSONObject(jsonString).getJSONArray("output-queue");
		
		for(int i = 0; i < toDissect.length(); i++) {
			JSONObject currentTable = (JSONObject) toDissect.get(i);
			
			String playerName = currentTable.getString("sender");
			String messageSent = currentTable.getString("data");
			String outputType = currentTable.getString("sendtype");
			//String timeSent = currentTable.getString("timeSent");
			
			//Date date = new Date(Long.parseLong(timeSent)*1000L); 
			//SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss z"); 
			//String formattedDate = sdf.format(date);
			
			Outputz newOutputThing = new Outputz(playerName, messageSent, outputType);
			outputs.put(numOutputs++, newOutputThing);
		}
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
	
	public void flushOutputs() {
        for (Entry<WebsocketClient, String> currentConnection : MainClass.authed.entrySet()) {
	        for (Entry<Integer, Outputz> currentOutput : outputs.entrySet()) {
	        	if(currentConnection.getKey().userName.equals(currentOutput.getValue().userName)) {
	        		try {
						currentConnection.getKey().outputTo(currentOutput.getValue().msg, currentOutput.getValue().outputType);
					} catch (Exception e) {
						e.printStackTrace(System.out);
					}
	        	}
	        }
        }
        numOutputs = 0;
        outputs.clear();
	}
	
	public void addChats(String jsonString) {
		try {
		JSONArray toDissect = new JSONObject(jsonString).getJSONArray("chat-data");
		
		for(int i = 0; i < toDissect.length(); i++) {
			JSONObject currentTable = (JSONObject) toDissect.get(i);
			
			String playerName = currentTable.getString("userName");
			String playerId = currentTable.getString("playerId");
			String messageSent = currentTable.getString("messageSent");;
			String timeSent = currentTable.getString("timeSent");
			
			Date date = new Date(Long.parseLong(timeSent)*1000L); 
			SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss z"); 
			String formattedDate = sdf.format(date);
			
			chatlogs.put(numLogs++, formattedDate+" "+playerName+": "+messageSent);
			MainClass.dbM.addChatLog(this.serverKey, playerName, playerId, timeSent, messageSent);
		}
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
	
	public void flushChats() {
        for (Entry<WebsocketClient, String> currentConnection : MainClass.authed.entrySet()) {
	        for (Entry<Integer, String> currentChat : chatlogs.entrySet()) {
	        	if(this.serverKey.equals(currentConnection.getKey().serverKey)) {
	        		try {
						currentConnection.getKey().outputTo(currentChat.getValue(), "print");
					} catch (Exception e) {
						e.printStackTrace(System.out);
					}
	        	}
	        }
        }
        numLogs = 0;
        chatlogs.clear();
	}
}
