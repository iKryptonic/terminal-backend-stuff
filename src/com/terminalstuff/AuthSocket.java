package com.terminalstuff;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ConcurrentModificationException;
import java.util.Map.Entry;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;

public class AuthSocket extends WebSocketServer {
	
	public AuthSocket( int port ) throws UnknownHostException {
		super( new InetSocketAddress( port ) );
	}

	public AuthSocket( InetSocketAddress address ) {
		super( address );
	}

	@Override
	public void onOpen( WebSocket conn, ClientHandshake handshake ) {
		WebsocketClient newClient = new WebsocketClient(conn);
		try {
			RandomString a = new RandomString();
			newClient.sendCode(a.randomAlphaNumeric(8));
		} catch (JSONException e) {
			e.printStackTrace(System.out);
		}
	}

	@Override
	public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
		try {
	        for (Entry<WebsocketClient, String> entry : MainClass.authed.entrySet()) {
	        	if(entry.getKey()!=null) {
					if(entry.getKey().cConn.equals(conn)) {
						entry.getKey().isDead = true;
						MainClass.authed.remove(entry.getKey());
					}
	        	}
	        }
		} catch(ConcurrentModificationException ign) {
			
		}
	}
 
	@Override
	public void onMessage( WebSocket conn, String message ) throws NullPointerException,StringIndexOutOfBoundsException {
        for (Entry<WebsocketClient, String> entry : MainClass.authed.entrySet()) {
            try {
            	if(!entry.getKey().isDead) {
					if(entry.getKey().cConn.equals(conn)) {
						String[] testCmd = message.split(" ");
						WebsocketClient cPlr = entry.getKey();			
						if((!testCmd[0].equals("help")) && (!testCmd[0].equals("cmds")) && (!testCmd[0].equals("commands")) && (!cPlr.hasBeenWelcomed)) {
							cPlr.outputTo("WELCOME "+cPlr.userName, "notice");
							cPlr.outputTo("--------------------------------------------------------------------------------", "print");
							cPlr.outputTo("--------------------------------------------------------------------------------", "print");
							cPlr.outputTo("Commands: ", "notice");
							cPlr.outputTo("switch [serverkey] - CHANGE SERVER", "run");
							cPlr.outputTo("servers - VIEW ALL SERVERS", "run");
							cPlr.outputTo("/[message] - GLOBAL MESSAGE", "run");
							cPlr.outputTo("connected - GET ALL CONNECTED CLIENTS", "run");
							cPlr.outputTo("commands - SHOW COMMANDS", "run");
							cPlr.outputTo("cmds - SHOW COMMANDS", "run");
							cPlr.outputTo("help - SHOW COMMANDS", "run");
							cPlr.outputTo("--------------------------------------------------------------------------------", "print");
							cPlr.outputTo("--------------------------------------------------------------------------------", "print");
							cPlr.hasBeenWelcomed = true;
						}
						if(testCmd[0].substring(0, 1).equals("/")) {
							if(testCmd.length>1) {
								testCmd[1] = testCmd[0].replace("/", "")+testCmd[1];
							}
							testCmd[0] = "/";
						}
						switch(testCmd[0]) {
						case "exit":
							conn.close();
							break;
						case "servers":
							cPlr.outputTo("--------------------------------------------------------------------------------", "print");
							for(Entry<String, Server> srv : MainClass.servers.entrySet()) {
								Server cSrv = srv.getValue();
								if(cSrv.serverKey.equals(cPlr.serverKey)) {
									cPlr.outputTo("<---- YOU ARE CURRENTLY CONNECTED TO THIS SERVER ---->", "warn");
								}
								cPlr.outputTo("Server ["+cSrv.serverKey+"] "+cSrv.place, "cmd");
								cPlr.outputTo("Players: ", "cmd");
								
								StringBuilder sPlayers = new StringBuilder();
								sPlayers.append("{ ");
								for(Entry<String, String> cPlrBuildr : cSrv.players.entrySet()) {
									sPlayers.append(cPlrBuildr.getKey() + " ");
								}
								sPlayers.append("}");
								cPlr.outputTo(sPlayers.toString(), "run");
								
								//cPlr.outputTo("JobID: "+cSrv.jobId, "cmd");
								cPlr.outputTo("--------------------------------------------------------------------------------", "print");
							}
							break;
						case "connected":
							cPlr.outputTo("--------------------------------------------------------------------------------", "print");
							for(Entry<WebsocketClient, String> authie : MainClass.authed.entrySet()) {
								WebsocketClient aws = authie.getKey();
								cPlr.outputTo("User: "+aws.userName, "cmd");
								cPlr.outputTo("Current Server Connection: "+aws.serverKey, "cmd");
								cPlr.outputTo("--------------------------------------------------------------------------------", "print");
							}
							break;
						case "switch":
							String newServer = testCmd[1];
							boolean serverFound = false;
							if(newServer!=null) {
								for(Entry<String, Server> currentServer : MainClass.servers.entrySet()) {
									Server cs = currentServer.getValue();
									String sKey = cs.serverKey;
									if(newServer.equals(sKey) && (cPlr.serverKey!=cs.serverKey)) {
										cPlr.serverKey = cs.serverKey;
										cPlr.jobId = cs.jobId;
										entry.setValue(cs.serverKey);
										cPlr.outputTo("Changed your server to ["+cPlr.serverKey+"]", "cmd");
										serverFound = true;
									} else if((newServer.equals(sKey)) && (cPlr.serverKey.equals(cs.serverKey))) {
										cPlr.outputTo("You're already connected to that server!", "error");
									}
								}
							} else {
								cPlr.outputTo("Incorrect command syntax, please use \"switch [serverkey]\"", "error");
							}
							if(!serverFound) {
								cPlr.outputTo("Server ["+newServer+"] not found!", "error");
							}
							break;
						case "/":
							for(Entry<String, Server> srv : MainClass.servers.entrySet()) {
								Server cSrv = srv.getValue();
								if(cSrv.serverKey.equals(cPlr.serverKey)) {
									cSrv.addNotification(cPlr.userName, message.substring(1));
								}
							}
						break;
						case "-global":
							for(Entry<String, Server> srv : MainClass.servers.entrySet()) {
								Server cSrv = srv.getValue();
								cSrv.addCommand(cPlr.userName, message.replace("-global ", ""));
							}
						break;
						case "cmds":
						case "help":
						case "commands":
							cPlr.hasBeenWelcomed = true;
							cPlr.outputTo("--------------------------------------------------------------------------------", "print");
							cPlr.outputTo("--------------------------------------------------------------------------------", "print");
							cPlr.outputTo("Commands: ", "notice");
							cPlr.outputTo("switch [serverkey] - CHANGE SERVER", "run");
							cPlr.outputTo("servers - VIEW ALL SERVERS", "run");
							cPlr.outputTo("/[message] - SERVER MESSAGE", "run");
							cPlr.outputTo("-global [command] [arguments] - GLOBAL COMMAND", "run");
							cPlr.outputTo(";[command] [arguments] - SERVER COMMAND", "run");
							cPlr.outputTo("connected - GET ALL CONNECTED CLIENTS", "run");
							cPlr.outputTo("commands - SHOW COMMANDS", "run");
							cPlr.outputTo("cmds - SHOW COMMANDS", "run");
							cPlr.outputTo("help - SHOW COMMANDS", "run");
							cPlr.outputTo("--------------------------------------------------------------------------------", "print");
							cPlr.outputTo("--------------------------------------------------------------------------------", "print");
						break;
						default:
							if(message.startsWith(";")) {
								MainClass.servers.get(cPlr.jobId).addCommand(cPlr.userName, message);
							} else {
								for(Entry<WebsocketClient, String> e : MainClass.authed.entrySet()) {
									e.getKey().outputTo(cPlr.userName+": "+message, "notice");
								}
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace(System.out);;
			}
        }
	}
	@Override
	public void onError( WebSocket conn, Exception ex ) {
		ex.printStackTrace();
		if( conn != null ) {
			// some errors like port binding failed may not be assignable to a specific websocket
		}
	}

	@Override
	public void onStart() {
		System.out.println(MainClass.CColor.GREEN_BOLD+"Terminal client websocket started!"+MainClass.CColor.RESET);
		setConnectionLostTimeout(0);
		setConnectionLostTimeout(100);
	}
}
