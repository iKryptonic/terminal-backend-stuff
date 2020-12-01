package com.terminalstuff;

import org.java_websocket.WebSocket;
import org.json.JSONException;
import org.json.JSONObject;

public class WebsocketClient {
	
	String userName;
	String userId;
	String verificationCode;
	String serverKey;
	String jobId;
	String lastMessage;
	WebSocket cConn;
	boolean hasBeenWelcomed = false;
	public boolean isDead = false;
	
	public WebsocketClient(WebSocket cCon) {
		this.cConn = cCon;
	}
	
	public void setData(String user, String id) {
		this.userName = user;
		this.userId = id;
	}
	
	public void sendCode(String code) throws JSONException {
		JSONObject jsonBuild = new JSONObject();
		jsonBuild.put("cmd", "0");
		jsonBuild.put("code", code);
		
		MainClass.authQueue.put(code, this);
		
		this.cConn.send(jsonBuild.toString());
	}
	
	public void confirmAuth(String user, String uid, String sKey, String jid, String verifyCode) throws JSONException {
		JSONObject jsonBuild = new JSONObject();
		jsonBuild.put("cmd", "1");
		jsonBuild.put("user", user);
		jsonBuild.put("id", uid);
		
		this.userId = uid;
		this.userName = user;
		this.serverKey = sKey;
		this.jobId = jid;
		this.verificationCode = verifyCode;
		
		this.cConn.send(jsonBuild.toString());
	}
	
	public void fireKeepAlive() throws JSONException, NullPointerException {
		if(this.isDead==false) {
			
			JSONObject jsonBuild = new JSONObject();
			jsonBuild.put("cmd", "2");
			
			this.cConn.send(jsonBuild.toString());
		}
	}
	
	// add ban analysis command for bot
	
	public void outputTo(String msg, String typ) throws JSONException {
		if(!this.isDead && msg != this.lastMessage) {
			this.lastMessage = msg;
			JSONObject jsonBuild = new JSONObject();
			jsonBuild.put("cmd", "3");
			jsonBuild.put("outputType", typ);
			jsonBuild.put("outputText", msg);
			if(msg.length() >= 1000) {
				jsonBuild.remove("outputText");
				jsonBuild.put("outputText", "[this message was extremely long]");
				this.lastMessage = "[this message was extremely long]";
			}
			
			this.cConn.send(jsonBuild.toString());
		}
	}
	
	public String toString() {
		return this.userName;
	}
	
}
