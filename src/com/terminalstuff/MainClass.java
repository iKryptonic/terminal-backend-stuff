package com.terminalstuff;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.terminalstuff.HTTPServer.ContextHandler;
import com.terminalstuff.HTTPServer.Request;
import com.terminalstuff.HTTPServer.Response;
import com.terminalstuff.HTTPServer.VirtualHost;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;

import com.terminalstuff.AuthSocket;

/* TODO:
 * still needs
 * - Messaging
 * - Ban Queueing or implemented ban stuff
 * - Report solving count (really tusks stuff to do)
 */

public class MainClass {
	
	public static enum CColor {
	    //Color end string, color reset
	    RESET("\033[0m"),

	    // Regular Colors. Normal color, no bold, background color etc.
	    BLACK("\033[0;30m"),    // BLACK
	    RED("\033[0;31m"),      // RED
	    GREEN("\033[0;32m"),    // GREEN
	    YELLOW("\033[0;33m"),   // YELLOW
	    BLUE("\033[0;34m"),     // BLUE
	    MAGENTA("\033[0;35m"),  // MAGENTA
	    CYAN("\033[0;36m"),     // CYAN
	    WHITE("\033[0;37m"),    // WHITE

	    // Bold
	    BLACK_BOLD("\033[1;30m"),   // BLACK
	    RED_BOLD("\033[1;31m"),     // RED
	    GREEN_BOLD("\033[1;32m"),   // GREEN
	    YELLOW_BOLD("\033[1;33m"),  // YELLOW
	    BLUE_BOLD("\033[1;34m"),    // BLUE
	    MAGENTA_BOLD("\033[1;35m"), // MAGENTA
	    CYAN_BOLD("\033[1;36m"),    // CYAN
	    WHITE_BOLD("\033[1;37m"),   // WHITE

	    // Underline
	    BLACK_UNDERLINED("\033[4;30m"),     // BLACK
	    RED_UNDERLINED("\033[4;31m"),       // RED
	    GREEN_UNDERLINED("\033[4;32m"),     // GREEN
	    YELLOW_UNDERLINED("\033[4;33m"),    // YELLOW
	    BLUE_UNDERLINED("\033[4;34m"),      // BLUE
	    MAGENTA_UNDERLINED("\033[4;35m"),   // MAGENTA
	    CYAN_UNDERLINED("\033[4;36m"),      // CYAN
	    WHITE_UNDERLINED("\033[4;37m"),     // WHITE

	    // Background
	    BLACK_BACKGROUND("\033[40m"),   // BLACK
	    RED_BACKGROUND("\033[41m"),     // RED
	    GREEN_BACKGROUND("\033[42m"),   // GREEN
	    YELLOW_BACKGROUND("\033[43m"),  // YELLOW
	    BLUE_BACKGROUND("\033[44m"),    // BLUE
	    MAGENTA_BACKGROUND("\033[45m"), // MAGENTA
	    CYAN_BACKGROUND("\033[46m"),    // CYAN
	    WHITE_BACKGROUND("\033[47m"),   // WHITE

	    // High Intensity
	    BLACK_BRIGHT("\033[0;90m"),     // BLACK
	    RED_BRIGHT("\033[0;91m"),       // RED
	    GREEN_BRIGHT("\033[0;92m"),     // GREEN
	    YELLOW_BRIGHT("\033[0;93m"),    // YELLOW
	    BLUE_BRIGHT("\033[0;94m"),      // BLUE
	    MAGENTA_BRIGHT("\033[0;95m"),   // MAGENTA
	    CYAN_BRIGHT("\033[0;96m"),      // CYAN
	    WHITE_BRIGHT("\033[0;97m"),     // WHITE

	    // Bold High Intensity
	    BLACK_BOLD_BRIGHT("\033[1;90m"),    // BLACK
	    RED_BOLD_BRIGHT("\033[1;91m"),      // RED
	    GREEN_BOLD_BRIGHT("\033[1;92m"),    // GREEN
	    YELLOW_BOLD_BRIGHT("\033[1;93m"),   // YELLOW
	    BLUE_BOLD_BRIGHT("\033[1;94m"),     // BLUE
	    MAGENTA_BOLD_BRIGHT("\033[1;95m"),  // MAGENTA
	    CYAN_BOLD_BRIGHT("\033[1;96m"),     // CYAN
	    WHITE_BOLD_BRIGHT("\033[1;97m"),    // WHITE

	    // High Intensity backgrounds
	    BLACK_BACKGROUND_BRIGHT("\033[0;100m"),     // BLACK
	    RED_BACKGROUND_BRIGHT("\033[0;101m"),       // RED
	    GREEN_BACKGROUND_BRIGHT("\033[0;102m"),     // GREEN
	    YELLOW_BACKGROUND_BRIGHT("\033[0;103m"),    // YELLOW
	    BLUE_BACKGROUND_BRIGHT("\033[0;104m"),      // BLUE
	    MAGENTA_BACKGROUND_BRIGHT("\033[0;105m"),   // MAGENTA
	    CYAN_BACKGROUND_BRIGHT("\033[0;106m"),      // CYAN
	    WHITE_BACKGROUND_BRIGHT("\033[0;107m");     // WHITE

	    private final String code;

	    CColor(String code) {
	        this.code = code;
	    }

	    @Override
	    public String toString() {
	        return code;
	    }
	}
	
	static Map<String, WebsocketClient> authQueue = new HashMap<String, WebsocketClient>();
	static Map<WebsocketClient, String> authed = new HashMap<WebsocketClient, String>();
	static Map<String, Server> servers = new HashMap<String, Server>();

	// New intents thing got added owo
	static JDA builtJDA;
	static QuickHTTPRequest makeReq = new QuickHTTPRequest();
	
	static JSONObject place1bans = null;
	static JSONObject place2bans = null;
	
	static String rawp1bans = "";
	static String rawp2bans = "";
	
	static DatabaseManager dbM = new DatabaseManager();
	
	public static String convert(InputStream inputStream) throws IOException {
	 
		StringBuilder stringBuilder = new StringBuilder();
		String line = null;
		
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {	
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line);
			}
		}
	 
		return stringBuilder.toString();
	}
	
	private static void purgeDuplicateUsers() {
		Map<WebsocketClient, String> toRemove = new HashMap<WebsocketClient, String>();
		
		for(Entry<WebsocketClient, String> entry : authed.entrySet()) {
			for(Entry<WebsocketClient, String> maybeDupe : authed.entrySet()) {
				if(!(entry.getKey().verificationCode.equals(maybeDupe.getKey().verificationCode))) {
					if(entry.getKey().userName.equals(maybeDupe.getKey().userName)) {
						if((toRemove.get(maybeDupe.getKey()) == null) && (toRemove.get(entry.getKey()) == null)) {
							toRemove.put(maybeDupe.getKey(), maybeDupe.getValue());
						}
					}
				}
			}
		}
		
		for(Entry<WebsocketClient, String> e : toRemove.entrySet()) {
			e.getKey().cConn.close();
		}
		
	}

	public static void main(String[] args) throws UnknownHostException {
		
		Properties prop = new Properties();
		String fileName = "/home/ikrypto/deploy/terminal.config";
		InputStream is = null;
		
	    try {
			is = new FileInputStream(fileName);
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	    try {
			prop.load(is);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	    
	    int port = Integer.parseInt(prop.getProperty("HttpServerPort"));
	    
		JDABuilder jda = JDABuilder.createLight(prop.getProperty("BotToken"), 
				GatewayIntent.DIRECT_MESSAGE_REACTIONS,
				GatewayIntent.DIRECT_MESSAGE_TYPING,
				GatewayIntent.DIRECT_MESSAGES,
				GatewayIntent.GUILD_BANS,
				GatewayIntent.GUILD_EMOJIS,
				GatewayIntent.GUILD_INVITES,
				GatewayIntent.GUILD_MEMBERS,
				GatewayIntent.GUILD_MESSAGE_REACTIONS,
				GatewayIntent.GUILD_MESSAGE_TYPING,
				GatewayIntent.GUILD_MESSAGES,
				GatewayIntent.GUILD_PRESENCES,
				GatewayIntent.GUILD_VOICE_STATES
				);
		
		final String authorizationKey = "veryCoolAuthKey";
		
		final AuthSocket s = new AuthSocket(20015);
		ReportsWebsocket rs = new ReportsWebsocket(20030);

		try {
            // set up server
            
            HTTPServer server = new HTTPServer(port);
            
            VirtualHost host = server.getVirtualHost(null); // default host
            host.setAllowGeneratedIndex(true); // with directory index pages
            host.addContext("/", new ContextHandler() {
                public int serve(Request req, Response resp) throws IOException {
                    resp.getHeaders().add("Content-Type", "text/plain");
                    resp.send(200, "This is the index. I think you were looking for /api/?");
                    return 0;
                }
            });
            host.addContext("/api/", new ContextHandler() {
                public int serve(Request req, Response resp) throws IOException {
                    resp.getHeaders().add("Content-Type", "text/plain");
                    resp.send(400, "We have a few APIs... What are you looking for here?");
                    return 0;
                }
            });
            host.addContext("/api/sb_tools", new ContextHandler() {
                public int serve(Request req, Response resp) throws IOException {
                    Map<String, String> params = req.getParams();
                    boolean cando = false;
                    
                    if(req.getHeaders().contains("Authorization")) {
                        String authKeySent = req.getHeaders().get("Authorization");
                        if(authKeySent.contentEquals(authorizationKey)) {
                        	String apiRequest = null;
                			if(params.containsKey("request")){
                				apiRequest = params.get("request");
                            	// Server cSrv = servers.get(params.get("jobId"));
                            	
                				switch(apiRequest) {
	                				case "altFound":
	                					TextChannel botChannel = builtJDA.getGuildById("206332604918530058").getTextChannelById("641395176673378314");
	                					SelfUser me = builtJDA.getSelfUser();
	                					
	    					        	EmbedBuilder embed = new EmbedBuilder();
	    					            embed.setAuthor(me.getName(), me.getAvatarUrl());
	    					            embed.setDescription("An alternate account was detected\nName:"+params.get("userName")+"\nUserID:"+params.get("userId"));
	    					            embed.setColor(new Color(0x4286F4));
	    					            
	    							    botChannel.sendMessage(embed.build()).queue();
	                					break;
                				}
                			cando = true;
	                	    } // no request found
                        } // auth key was bad
                    } // no auth key at all
                    
                    resp.getHeaders().add("Content-Type", "text/plain");
                    if(cando==true) {
                    	resp.send(200, "Yep");
                    } else {
                    	resp.send(200, "Nope");
                    }
                    return 0;
                }
            });
            host.addContext("/api/getbans", new ContextHandler() {
                public int serve(Request req, Response resp) throws IOException {
                    Map<String, String> params = req.getParams();
                    String rtn = "Nope";
                    
        			if(params.containsKey("place")){
        				switch(params.get("place")) {
        				case "1":
        					rtn = rawp1bans;
        					break;
        				case "2":
        					rtn = rawp2bans;
        					break;
        				default:
        					rtn = "{\"error\":\"that place doesnt exist\"}";
        				}
        			}
                    
                    resp.getHeaders().add("Content-Type", "text/plain");
                    resp.send(200, rtn);
                    return 0;
                }
            });
            host.addContext("/api/auth", new ContextHandler() {
                public int serve(Request req, Response resp) throws IOException {
                    Map<String, String> params = req.getParams();
                    boolean cando = false;
                    
                    if(req.getHeaders().contains("Authorization")) {
                        String authKeySent = req.getHeaders().get("Authorization");
                        if(authKeySent.contentEquals(authorizationKey)) {
                        	String jobId = null;
                			String serverKey = null;
                			String authCode = null;
                			String userId = null;
                			String userName = null;
                			
                        	for (Map.Entry<String, String> entry : params.entrySet()) {
                        	    if(entry.getKey().equals("jobId")) {
                        	    	jobId = entry.getValue().toString();
                        	    } else if(entry.getKey().equals("serverKey")) {
                        	    	serverKey = entry.getValue().toString();
                        	    } else if(entry.getKey().equals("authCode")) {
                        	    	authCode = entry.getValue().toString();
                        	    } else if(entry.getKey().equals("userName")) {
                        	    	userName = entry.getValue().toString();
                        	    } else if(entry.getKey().equals("userId")) {
                        	    	userId = entry.getValue().toString();
                        	    } else {
                        	    	System.out.println("Unhandled param type:" + entry.getKey());
                        	    }
                        	}
                        	
                        	if(authQueue.containsKey(authCode)) {
                        		WebsocketClient foundYa = authQueue.get(authCode);
                        		try {
									foundYa.confirmAuth(userName, userId, serverKey, jobId, authCode);
									cando = true;
									authed.put(foundYa, serverKey);
									authQueue.remove(authCode);
								} catch (Exception e) {
									e.printStackTrace(System.out);
								}
                        	}
                        	
                        }
                    }
                    
                    resp.getHeaders().add("Content-Type", "text/plain");
                    
                    if(cando==true) {
                    	resp.send(200, "Yep");
                    } else {
                    	resp.send(200, "Nope");
                    }
                    return 0;
                }
            });
            host.addContext("/api/logs", new ContextHandler() {
                public int serve(Request req, Response resp) throws IOException {
                	Map<String, String> params = req.getParams();
                	String rtn = "Nope";
                	int code = 400;
                	
                	if(req.getHeaders().contains("Authorization")) {
                		String authKeySent = req.getHeaders().get("Authorization");
                		if(authKeySent.contentEquals(authorizationKey)) {
                			
                			String searchTerm = null;
                			String serverKey = null;
                			String userName = null;
                			
                        	for (Map.Entry<String, String> entry : params.entrySet()) {
                        	    if(entry.getKey().equals("searchTerm")) {
                        	    	searchTerm = entry.getValue().toString();
                        	    } else if(entry.getKey().equals("serverKey")) {
                        	    	serverKey = entry.getValue().toString(); 
                        	    } else if(entry.getKey().equals("userName")) {
                        	    	userName = entry.getValue().toString(); 
                        	    } else	{
                        	    	System.out.println("Unhandled param type:" + entry.getKey());
                        	    }
                        	}
                        	
                        	if(searchTerm!=null || serverKey!=null || userName!=null) {
                        		ResultSet testSQLReq = null;
                        		try {
                        			String queryString = "SELECT serverkey,username,userid,time,message FROM terminal_logging.chat_logs ";
                        			PreparedStatement pstmt = null;
                        			if(serverKey!=null) { // finish conditional statements!
                        				if(userName!=null && searchTerm != null) { // if we have serverkey, and username, and a search term
                        					queryString = queryString.concat("WHERE message LIKE ? AND serverkey = ? AND userName = ?;");
	                               			pstmt = dbM.newSQL.conn.prepareStatement( queryString );
	                                		pstmt.setString(1, "%"+searchTerm+"%");
	                                		pstmt.setString(2, serverKey);
	                                		pstmt.setString(3, userName);
                        				} else if(searchTerm!=null) { // if we have a serverkey and a search term
                        					queryString = queryString.concat("WHERE message LIKE ? AND serverkey = ?;");
	                               			pstmt = dbM.newSQL.conn.prepareStatement( queryString );
	                               			pstmt.setString(1, "%"+searchTerm+"%");
	                               			pstmt.setString(2, serverKey);
                        				} else if(userName!=null) { // if we have a serverkey and a username
                        					queryString = queryString.concat("WHERE serverkey = ? AND username = ?;");
	                               			pstmt = dbM.newSQL.conn.prepareStatement( queryString );
	                               			pstmt.setString(1, serverKey);
	                               			pstmt.setString(2, userName);
	                    				}
                        			} else {
                        				if(userName!=null && searchTerm!=null) { // no serverkey, just message and username
                        					queryString = queryString.concat("WHERE message LIKE ? AND username = ?;");
	                               			pstmt = dbM.newSQL.conn.prepareStatement( queryString );
	                               			pstmt.setString(1, "%"+searchTerm+"%");
	                               			pstmt.setString(2, userName);
                        				} else if(searchTerm!=null) { // no username or server key, just a message
                        					queryString = queryString.concat("WHERE message LIKE ?;");
	                               			pstmt = dbM.newSQL.conn.prepareStatement( queryString );
	                               			pstmt.setString(1, "%"+searchTerm+"%");
                        				} else if(userName!=null) { // no message or serverkey, just a user
	                        				queryString = queryString.concat("WHERE username = ?;");
	                               			pstmt = dbM.newSQL.conn.prepareStatement( queryString );
	                               			pstmt.setString(1, userName);
                        				}
                        			}
                        			testSQLReq = pstmt.executeQuery();
								} catch (Exception e) {
									e.printStackTrace(System.out);
								}
                        		
                        		if(testSQLReq!=null) {
                        			JSONObject toRTN = null;
                        			try {	                        				
                        				toRTN = new JSONObject();
                        				int i = 0;
	                        			while(testSQLReq.next()) {
	                        				String skey = testSQLReq.getString(1);
	                        				String user = testSQLReq.getString(2);
	                        				String uid = testSQLReq.getString(3);
	                        				String tstamp = testSQLReq.getString(4);	
	                        				String message = testSQLReq.getString(5);	

	                        				Map<String,String> bigReturn = new HashMap<String,String>();
	                        				bigReturn.put("serverkey", skey);
	                        				bigReturn.put("username", user);
	                        				bigReturn.put("userid", uid);
	                        				bigReturn.put("timestamp", tstamp);
	                        				bigReturn.put("message", message);
	                        				toRTN.put(Integer.toString(i), bigReturn);
	                        				i++;

	                        			}
                        			} catch(Exception e) {
                        				e.printStackTrace(System.out);
                        			}
                        			if(toRTN!=null) {
	                        			rtn = toRTN.toString();
	                        			code = 200;
                        			}
                        		}
                        		
                        	} else {
                        		rtn = "Nope";
                        		code = 400;
                        	}
                		}
                	}
                	
                    resp.getHeaders().add("Content-Type", "text/plain");
                    resp.send(code, rtn);
                    return 0;
                }
            });
            host.addContext("/api/moddata", new ContextHandler() {
                public int serve(Request req, Response resp) throws IOException {
                	Map<String, String> params = req.getParams();
                	String rtn = "Nope";
                	int code = 400;
                	
                	if(req.getHeaders().contains("Authorization")) {
                		String authKeySent = req.getHeaders().get("Authorization");
                		if(authKeySent.contentEquals(authorizationKey)) {
                			
                			String modName = null;
                			String week = null;
                			
                        	for (Map.Entry<String, String> entry : params.entrySet()) {
                        	    if(entry.getKey().equals("modName")) {
                        	    	modName = entry.getValue().toString();
                        	    } else if(entry.getKey().equals("week")) {
                        	    	week = entry.getValue().toString(); 
                        	    } else	{
                        	    	System.out.println("Unhandled param type:" + entry.getKey());
                        	    }
                        	}
                        	
                        	String testReq = makeReq.makeRequest("https://api.roblox.com/users/get-by-username?username="+modName);
                        	
                        	if((testReq!=null) && (testReq!="bad request")) {
                        		try {
                        			JSONObject testJSON = new JSONObject(testReq);
                        			modName = Integer.toString(testJSON.getInt("Id"));
                        		} catch(Exception e) {
                        			modName = null;
                        			e.printStackTrace(System.out);
                        		}
                        	}
                        	if(modName!=null) {
                        		ResultSet testSQLReq = null;
                        		try {
                        			String tbl = "";
                        			if(week==null) {
                        				tbl = "time_logging"; 
                        			} else {
                        				tbl = "time_logging_week"+week; 
                        			}
                        			String queryString = "SELECT placeid,joinTime,leaveTime,timeSpent FROM rblx_api."+tbl+" WHERE userId=?;";
                        			PreparedStatement pstmt = dbM.newSQL.conn.prepareStatement( queryString );
                        			pstmt.setString(1, modName);
                        			testSQLReq = pstmt.executeQuery();
								} catch (Exception e) {
									e.printStackTrace(System.out);
								}
                        		
                        		if(testSQLReq!=null) {
                        			JSONObject toRTN = null;
                        			try {	                        				
                        				toRTN = new JSONObject();
                        				int i = 0;
	                        			while(testSQLReq.next()) {
	                        				String placeId = Integer.toString(testSQLReq.getInt(1));
	                        				String joinTime = Integer.toString(testSQLReq.getInt(2));
	                        				String leaveTime = Integer.toString(testSQLReq.getInt(3));
	                        				String timeSpent = Integer.toString(testSQLReq.getInt(4));	

	                        				Map<String,String> bigReturn = new HashMap<String,String>();
	                        				bigReturn.put("placeId", placeId);
	                        				bigReturn.put("joinTime", joinTime);
	                        				bigReturn.put("leaveTime", leaveTime);
	                        				bigReturn.put("timeSpent", timeSpent);
	                        				toRTN.put(Integer.toString(i), bigReturn);
	                        				i++;

	                        			}
                        			} catch(Exception e) {
                        				e.printStackTrace(System.out);
                        			}
                        			if(toRTN!=null) {
	                        			rtn = toRTN.toString();
	                        			code = 200;
                        			}
                        		}
                        		
                        	} else {
                        		rtn = "Nope";
                        		code = 400;
                        	}
                		}
                	}
                	
                    resp.getHeaders().add("Content-Type", "text/plain");
                    resp.send(code, rtn);
                    return 0;
                }
            });
            host.addContext("/api/bans", new ContextHandler() {
                public int serve(Request req, Response resp) throws IOException {
                	Map<String, String> params = req.getParams();
                	
                	if(req.getHeaders().contains("Authorization")) {
                		String authKeySent = req.getHeaders().get("Authorization");
                		if(authKeySent.contentEquals(authorizationKey)) {
                			
                			String jobId = null;
                			
                        	for (Map.Entry<String, String> entry : params.entrySet()) {
                        	    if(entry.getKey().equals("jobId")) {
                        	    	jobId = entry.getValue().toString();
                        	    } else {
                        	    	System.out.println("Unhandled param type:" + entry.getKey());
                        	    }
                        	}
                        	
                        	if(jobId!=null) {
                        		if(servers.containsKey(jobId)) {
                        			
                        			Server recvServer = servers.get(jobId);
                        			
	                    			String body = "";
	                    			
	                    			StringBuilder stringBuilder = new StringBuilder();
	                    			
	                    			try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(req.getBody()))) {	
	                    				while ((body = bufferedReader.readLine()) != null) {
	                    					stringBuilder.append(body);
	                    				}
	                    			}
	                    			
	                    			body = stringBuilder.toString();
	                    			
	                    			try {
		                    			switch(recvServer.place) {
		                    			case"Place 1":
		                    				rawp1bans = body;
		                    				place1bans = new JSONObject(body);
		                    				break;
		                    			case"Place 2":
		                    				rawp2bans = body;
		                    				place2bans = new JSONObject(body);
		                    				break;
		                    			default:
		                    				// this is a bad server or unrecognized
		                    			}
	                    			} catch(JSONException ex) {
	                    				// unhandled, means ban table was too big
	                    			}
	                    				
                        		}
                        	}
                		}
                	}
                	
                    resp.getHeaders().add("Content-Type", "text/plain");
                    resp.send(200, "Yep");
                    return 0;
                }
            });
            host.addContext("/api/update", new ContextHandler() { // this is a keepalive for servers
                public int serve(Request req, Response resp) throws IOException {
                	Map<String, String> params = req.getParams();
                	
                	String rtn = "";
                	
                	if(req.getHeaders().contains("Authorization")) {
                		String authKeySent = req.getHeaders().get("Authorization");
                		if(authKeySent.contentEquals(authorizationKey)) {
                			
                			String jobId = null;
                			String serverKey = null;
                			String placeId = null;
                			
                        	for (Map.Entry<String, String> entry : params.entrySet()) {
                        	    if(entry.getKey().equals("jobId")) {
                        	    	jobId = entry.getValue().toString();
                        	    } else if(entry.getKey().equals("serverKey")) {
                        	    	serverKey = entry.getValue().toString();
                        	    } else if(entry.getKey().equals("placeId")) {
                        	    	placeId = entry.getValue().toString();
                        	    } else {
                        	    	System.out.println("Unhandled param type:" + entry.getKey());
                        	    }
                        	}
                        	
                        	if(jobId!=null) {
                        		if(servers.containsKey(jobId)) {
                        			String body = "";
                        			
                        			StringBuilder stringBuilder = new StringBuilder();
                        			
                        			try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(req.getBody()))) {	
                        				while ((body = bufferedReader.readLine()) != null) {
                        					stringBuilder.append(body);
                        				}
                        			}
                        			
                        			body = stringBuilder.toString();
                        			
                        			try {
										rtn = servers.get(jobId).getCommands();
									} catch (Exception e1) {
										e1.printStackTrace(System.out);
									}
                        			servers.get(jobId).updateTime();
                        			try {
										servers.get(jobId).updatePlayers(body);
										servers.get(jobId).addChats(body);
										servers.get(jobId).addOutputs(body);
									} catch (Exception e) {
										e.printStackTrace(System.out);;
									}
                        			servers.get(jobId).flushChats();
									servers.get(jobId).flushOutputs();
                                    for (Entry<WebsocketClient, String> entry : authed.entrySet()) {
                                    	if(entry.getValue().equals(serverKey)) {
                                    		try {
                                    			if(entry.getKey().cConn.isOpen()) {
                                    				entry.getKey().fireKeepAlive();
                                    			} else {
                                    				authed.remove(entry.getKey());
                                    			}
											} catch (Exception e) {
												e.printStackTrace(System.out);;
											}
                                    	}
                                    }
                        		} else {
									Server coolServer = new Server(jobId, serverKey, placeId);
                        			servers.put(jobId, coolServer);
                        		}
                        	}
                        	
                        	
                		}
                	}
                	
                	if(rtn.equals("")) {
                		rtn = "Server Created";
                	}
                	
                    resp.getHeaders().add("Content-Type", "text/plain");
                    resp.send(200, rtn);
                    return 0;
                }
            });
            server.start();
            System.out.println(CColor.GREEN_BOLD_BRIGHT + "HTTPServer is listening on port " + port + CColor.RESET);

    		s.start();
    		// rs.init();
    		/* not initializing this until its finished as it wastes some valuable websocket bandwidth space and such */
	        
    		builtJDA = jda.build();
	        builtJDA.addEventListener(new MessageListener());
	        servers.put("JDA2019", new Server("placeholder-server","JDA2019","7896557"));// make dummy server
	        servers.get("JDA2019").players.put("THIS_IS_A_PLACEHOLDER_SERVER", "JDA2019");
            
            try {
	            while(true) { // clean out bad servers
	                Iterator<Entry<String, Server>> it = servers.entrySet().iterator();
	                long cTime = System.currentTimeMillis() / 1000L;
	                
	                while (it.hasNext()) {
	                    Entry<String, Server> pair = it.next();
	                    long reviveDelay = (cTime-pair.getValue().lastUpdate);
	                    if((reviveDelay >= 40) && (pair.getValue().jobId!="placeholder-server")) { // 40 seconds passed
	                    	if(pair.getValue().isDead != true) {
	                    		pair.getValue().killServer();
	                    		it.remove(); // avoids a ConcurrentModificationException
	                    	}
	                    }
	                }
                    for (Entry<WebsocketClient, String> entry : authed.entrySet()) {
                    	if(entry.getValue().equals("JDA2019")) { // dummi server
                    		try {
                    			if(entry.getKey().cConn.isOpen()) {
                    				entry.getKey().fireKeepAlive();
                    			} else {
                    				authed.remove(entry.getKey());
                    			}
							} catch (Exception e) {
								e.printStackTrace(System.out);;
							}
                    	}
                    }
	                purgeDuplicateUsers();
	            Thread.sleep((1000));
	            }
			} catch (Exception ex) {
				ex.printStackTrace(System.out);
			} // end the forever loop thread
			
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
		
		
		
	}

}
