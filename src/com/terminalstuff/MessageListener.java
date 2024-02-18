package com.terminalstuff;

import java.awt.Color;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AccountManager;

public class MessageListener extends ListenerAdapter
{
   private Globals buildGlobals() {
   	Globals globals = JsePlatform.standardGlobals();			
	
	   	// send stuff to lua for eval
	   	globals.set("runningServers", CoerceJavaToLua.coerce(MainClass.servers));
	   	globals.set("runSQL", CoerceJavaToLua.coerce(new DatabaseManager()));
	   	globals.set("authedUsers", CoerceJavaToLua.coerce(MainClass.authed));
	   	globals.set("httpAPI", CoerceJavaToLua.coerce(new QuickHTTPRequest()));
	   	// ok don.

       return globals;
   }
	   
    @Override
    public void onMessageReceived(MessageReceivedEvent event) throws StringIndexOutOfBoundsException
    {
    	if(!event.getAuthor().isBot()) {
	        Message msg = event.getMessage();
	        SelfUser me = MainClass.builtJDA.getSelfUser();
	        AccountManager acct = me.getManager();
        	BanHandler lookfor = new BanHandler();
    		QuickHTTPRequest makeReq = new QuickHTTPRequest();
    		
    		
	        
	        if (msg.getChannelType().isGuild()) {
		          
		        MessageChannel channel = event.getChannel();
		        Guild guild = null;
		        
		        guild = event.getGuild();
		        
		        if(guild.getId().equals("206332604918530058") || guild.getId().equals("640739710179737610")) { // vsb mods only
		        	// First ID is Infinity Devs, Second ID is Compy Bois
		        	List<Role> roles = msg.getMember().getRoles();
		        	int modLevel = 0;
		        	if(roles.contains(guild.getRoleById("464249360117530636"))) {
		        		modLevel = 1; // has SB Moderator role
		        	}
		        	if(roles.contains(guild.getRoleById("464249333911388170"))) {
		        		modLevel = 2; // has Moderator role
		        	}
		        	if(roles.contains(guild.getRoleById("464249304656379907"))) {
		        		modLevel = 3; // has Administrator role
		        	}
		        	
		        	if(event.getAuthor().getId().equals("148931616452902912")) // is darkus
		        		modLevel = 5;
		        	
		        	boolean isCmd = false;
		        	
		        	String sMsg = msg.getContentRaw();
		        	String[] cMsg = sMsg.split(" ");
		        	if((cMsg.length>0)) {
						if(cMsg[0].startsWith("!")) {
								cMsg[0] = cMsg[0].replace("!", "");
								isCmd = true;
						}
						if(isCmd) {
					        if (cMsg[0].equals("ping")){
					            long time = System.currentTimeMillis();
					            channel.sendMessage("Pong!")
					                   .queue(response -> {
					                       response.editMessageFormat("Pong: %d ms", System.currentTimeMillis() - time).queue();
					                   });
							    channel.deleteMessageById(event.getMessageId()).complete();
					        } else if ((cMsg[0].equals("auth") || cMsg[0].equals("authenticate")) && modLevel > 0){
					        	String authCode = cMsg[1];
					        	String userId = null;
					        	String userName = null;
					    		
					    		String req = makeReq.makeRequest("http://api.tuskor661.com/discord/whois?userId="+event.getAuthor().getId());
					    		
					    		if(req!=null && req!="request failed") {
					    			try {
						    			JSONObject coolJSON = new JSONObject(req);
						    			userId = Integer.toString(coolJSON.getInt("robloxId"));
										userName = coolJSON.getString("robloxUsername");
				                    	if(MainClass.authQueue.containsKey(authCode)) {
				                    		WebsocketClient foundYa = MainClass.authQueue.get(authCode);
				                    		try {
												foundYa.confirmAuth(userName, userId, "JDA2019", "placeholder-server", authCode);
												MainClass.authed.put(foundYa, "JDA2019");
												MainClass.authQueue.remove(authCode);
												try {
										            event.getAuthor().openPrivateChannel().complete().sendMessage("Hey there "+userName+", authentication was successful!").queue();
												} catch(ErrorResponseException ignored) {
					                    			
					                    		}
												
											} catch (Exception e) {
												e.printStackTrace(System.out);
											}
				                    	}    
					    			} catch(JSONException ex) {
					    				ex.printStackTrace(System.out);
					    			}
					    		}
		                    channel.deleteMessageById(event.getMessageId()).complete();
		                    // end !auth
					        } else if ((cMsg[0].equals("setname")) && modLevel > 4){
					        	acct.setName(cMsg[1]).complete();
					            event.getAuthor().openPrivateChannel().complete().sendMessage("I set my name to "+cMsg[1]).queue();
							    channel.deleteMessageById(event.getMessageId()).complete();
					        // end !setname
					        } else if ((cMsg[0].equals("getguilds")) && modLevel > 4){
					        	EmbedBuilder embed = new EmbedBuilder();
								embed.setAuthor(me.getName(), me.getAvatarUrl());
								
					        	me.getJDA().getGuilds()
					        	.stream()
					        	.forEach(g -> 
					            {
					            	embed.addField(g.getName(),
					            					g.getId(),
					            							 true);
					            });
								embed.setFooter("Made by alucard#8668 (148931616452902912)", null);
								embed.setColor(new Color(0x4286F4));
								channel.sendMessageEmbeds(embed.build()).queue();
					        // end !getguilds
					        } else if ((cMsg[0].equals("setnick")) && modLevel > 4){
					        	event.getGuild().getMemberById(me.getId()).modifyNickname(cMsg[1]).queue();
					            event.getAuthor().openPrivateChannel().complete().sendMessage("I set my nick to "+cMsg[1]).queue();
							    channel.deleteMessageById(event.getMessageId()).complete();
					        // end !setnick
					        } else if ((cMsg[0].equals("ev"))){
					        	
					        	cMsg[0] = "";
					        	
					        	EmbedBuilder embed = new EmbedBuilder();
					            embed.setAuthor(me.getName(), me.getAvatarUrl());
					            embed.setFooter("Made by alucard#8668 (148931616452902912)", null);
					            embed.setColor(new Color(0x4286F4));
				    
					        	try {
					        		
					        		Globals globals = buildGlobals();
					        		
					        		globals.set("me", CoerceJavaToLua.coerce(me));
					        		globals.set("event", CoerceJavaToLua.coerce(event));
					        		globals.set("guild", CoerceJavaToLua.coerce(guild));
					        		globals.set("channel", CoerceJavaToLua.coerce(channel));
					        		globals.set("author", CoerceJavaToLua.coerce(event.getAuthor()));
					        		
					        		
						        	String eval_string = String.join(" ", cMsg); // store the rest of the array as if it's meant to be evaluated
						        	
						        	LuaValue chunk = globals.load("return function() "
						        										+eval_string+
						        										" end");
					        	
					        		
						        	LuaValue result = chunk.call().call();
						        	
						        	String output = chunk.tojstring();
					        	
						            if(output != null && result.tojstring() != "nil"){
						            	embed.setDescription("***GOT***\n"+result.tojstring());
						            } else {
						            	embed.setDescription("***NOTHING RETURNED***");
						            }
					            
					        	} catch (LuaError e) {
					        		
					            	embed.setDescription("*ERROR* - \n"+e.getLocalizedMessage());
					            	// e.printStackTrace(); // this is for debug don't bother uncommenting
					            	
					        	}
					            channel.sendMessageEmbeds(embed.build()).queue();

					        // end !ev
					        } else if ((cMsg[0].equals("getban")) && modLevel >=0){
					        	EmbedBuilder embed = new EmbedBuilder();
					            embed.setAuthor(me.getName(), me.getAvatarUrl());
					            embed.setFooter("Made by alucard#8668 (148931616452902912)", null);
					            embed.setColor(new Color(0x4286F4));
					        	if(cMsg.length==3) {
									String banStuff = null;
							        try {
										banStuff = lookfor.getBanByName(cMsg[1], cMsg[2]);
									} catch (JSONException e) {
										e.printStackTrace(System.out);
									}
							       	if((banStuff!=null) && (banStuff!="")) {
							       		embed.setDescription("**BAN DATA**\n"+banStuff);
							       	} else {
							       		embed.setDescription("**BAN DATA**\nNo data found for "+cMsg[2]);
							       	}
					        	} else {
						            embed.setDescription("hey bad syntax.\nuse a cool syntax like '!getban 1 Darkus_Theory'\nSyntax: !getban [place] [name]");
					        	}
							    event.getAuthor().openPrivateChannel().complete().sendMessageEmbeds(embed.build()).queue();
							    channel.deleteMessageById(event.getMessageId()).complete();
					        // end !getban
					        } else if ((cMsg[0].equals("bancount")) && modLevel >=0){
					        	EmbedBuilder embed = new EmbedBuilder();
					            embed.setAuthor(me.getName(), me.getAvatarUrl());
					            embed.setFooter("Made by alucard#8668 (148931616452902912)", null);
					            embed.setColor(new Color(0x4286F4));
					        	if(cMsg.length==2) {
										String[] banStuff = null;
							        	try {
											banStuff = lookfor.getBanCountByMod(cMsg[1]);
										} catch (JSONException e) {
											e.printStackTrace(System.out);
										}
							        	if((banStuff!=null) && (banStuff[0]!="bad request")) {
								            embed.addField("Bans for moderator", cMsg[1], true);
								            embed.addField("Place 1 ban count", banStuff[0], true);
								            embed.addField("Place 2 ban count", banStuff[1], true);
							        	} else if((banStuff!=null) && (banStuff[0].equals("bad request"))){
							        		embed.setDescription(banStuff[1]);
							        	} else {
								            embed.setDescription("**BAN DATA**\nNo data found for "+cMsg[1]);
							        	}
					        	} else {
						            embed.setDescription("hey bad syntax.\nuse a cool syntax like '!bancount Darkus_Theory'\nSyntax: !bancount [name]");
					        	}
							    event.getAuthor().openPrivateChannel().complete().sendMessageEmbeds(embed.build()).queue();
							    channel.deleteMessageById(event.getMessageId()).complete();
					        // end !bancount
					        } else if ((cMsg[0].equals("info")) && modLevel >= 0){
					            long guildCount = MainClass.builtJDA.getGuilds().size();
					            
								StringBuilder cUsrs = new StringBuilder();
								for(Entry<WebsocketClient, String> cPlrBuildr : MainClass.authed.entrySet()) {
									cUsrs.append(cPlrBuildr.getKey().userName + " ");
								}
					        	
					        	EmbedBuilder embed = new EmbedBuilder();
					            embed.setAuthor(me.getName(), me.getAvatarUrl());
					            embed.addField("Guild Count", Long.toString(guildCount), true);
					            embed.addField(String.format("[%d] Connected user(s)",MainClass.authed.size()),cUsrs.toString(), true);
					            embed.addField("Connected ROBLOX servers",Integer.toString(MainClass.servers.size()), true);
					            embed.setFooter("Made by alucard#8668 (148931616452902912)", null);
					            embed.setColor(new Color(0x4286F4));
					            
							    channel.deleteMessageById(event.getMessageId()).complete();
					            channel.sendMessageEmbeds(embed.build()).queue();
					        // end coolembed
							} else if ((cMsg[0].equals("cmds") || cMsg[0].equals("commands") || cMsg[0].equals("help")) && modLevel >= 0){
					        	
					        	EmbedBuilder embed = new EmbedBuilder();
					            embed.setAuthor(me.getName(), me.getAvatarUrl());
					            embed.setDescription("**Commands**\nAll prefixes are !\ncmds, commands, help\ninfo\nauth\nsetname\nsetnick\nping\nglobalcommand, gc, global\ngetban [place number] [name]\nbancount [modname]");
					            embed.setFooter("Made by alucard#8668 (148931616452902912)", null);
					            embed.setColor(new Color(0x4286F4));
							    channel.deleteMessageById(event.getMessageId()).complete();
							    event.getAuthor().openPrivateChannel().complete().sendMessageEmbeds(embed.build()).queue();
					        // end cmds
							} else if ((cMsg[0].equals("globalcommand") || cMsg[0].equals("global") || cMsg[0].equals("gc")) && modLevel >= 1){
								
					        	String userName = null;
					    		
					    		String req = makeReq.makeRequest("http://api.tuskor661.com/discord/whois?userId="+event.getAuthor().getId());
					    		
					    		try {
					    			JSONObject coolJSON = new JSONObject(req);
									userName = coolJSON.getString("robloxUsername");
					    		} catch(JSONException ex) {
					    			ex.printStackTrace(System.out);
					    		}
					    		
					    		if(req!=null && req!="request failed") {
						    		if(userName!=null) {
							        	for(Entry<String, Server> entry : MainClass.servers.entrySet()) {
							        		entry.getValue().addCommand(userName, sMsg.replace("!"+cMsg[0]+" ", ""));
							        	}
						    		}
					    		}
							    channel.deleteMessageById(event.getMessageId()).complete();
								
					        // end globalcommand
							}
						}
	        		}
	        	}
	        } else {
	        	event.getChannel().sendMessage("Please message me from the Infinity Developers Discord. https://discord.gg/uWnKT8anXf").queue();
	        }
        }
    }
}