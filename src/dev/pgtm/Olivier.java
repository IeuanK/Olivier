package dev.pgtm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;
import org.jibble.pircbot.Colors;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class Olivier extends PircBot
{
	private OBot instance;
	private ConfigReader configReader;
	private ConfigReader lastSeen;
	private HashMap<String, java.util.Date> lastSeenHM;
	private List<String> banList;
	private List<String> muteList;
	private Long lastSave;
	private User[] currentUsers;
	private String topic = null;
	
	public Olivier(OBot instance) {
		this.instance = instance;
		this.setName(instance.getName());
	}
	
	public HashMap<String, java.util.Date> loadLastSeen() {
		Integer num = 0;
		HashMap<String, java.util.Date> empty = new HashMap<String, java.util.Date>();
		for(String key : this.lastSeen.getKeys()) {
			Date date = new Date();
			date.setTime(Long.parseLong(this.lastSeen.loadProperty(key)));
			empty.put(key.toLowerCase(), date);
			num++;
		}
		System.out.println(num + " users lastseen loaded");
		return empty;
	}
	
	public void loadMuted() {
        String muted = this.configReader.loadProperty("muted");
        this.muteList = new ArrayList<String>();
        if(muted.contains(",")) {
        	String[] muteds = muted.split(",");
        	for(String o : muteds) {
        		this.muteList.add(o);
        		System.out.println(o + " added to mutelist");
        	}
        } else {
    		this.muteList.add(muted);
    		System.out.println(muted + " added to mutelist");
        }
        String banned = this.configReader.loadProperty("banned");
        this.banList = new ArrayList<String>();
        if(banned.contains(",")) {
        	String[] banneds = banned.split(",");
        	for(String o : banneds) {
        		this.banList.add(o);
        		System.out.println(o + " added to banlist");
        	}
        } else {
    		this.banList.add(banned);
    		System.out.println(banned + " added to banlist");
        }
	}
	
	public void saveMuted() {
		StringBuffer buffer = new StringBuffer();
		Boolean first = true;
		for(String user : this.muteList) {
			if(user.equals("Unset"))
				continue;
			
			System.out.println("User "+ user+ " added to muted");
			if(first) {
				buffer.append(user);
				first = false;
			} else {
				buffer.append("," + user);
			}
		}
		this.configReader.writeProperty("muted", buffer.toString());
        StringBuffer bBuffer = new StringBuffer();
        Boolean bFirst = true;
        for(String user : this.banList) {
            if(user.equals("Unset"))
                continue;
            
            System.out.println("User "+ user+ " added to banned");
            if(bFirst) {
                bBuffer.append(user);
                bFirst = false;
            } else {
                bBuffer.append("," + user);
            }
        }
        this.configReader.writeProperty("banned", buffer.toString());
	}
	
	public void saveLastSeen() {
		Integer num = 0;
		for(Entry<String, Date> e : this.lastSeenHM.entrySet()) {
			Date d = e.getValue();
			this.lastSeen.writeProperty(e.getKey().toLowerCase(), d.getTime() + "");
			num++;
		}
		this.lastSeen.writeObject();

		if(OBot.isDebug) {
			System.out.println(num + " users lastseen saved");
		}
	}
	
	public void setConfigReader(ConfigReader configReader) {
		this.configReader = configReader;
	}
	public ConfigReader getConfigReader() {
		return this.configReader;
	}
	
	public void setLastSeen(ConfigReader lastSeen) {
		this.lastSeen = lastSeen;
		this.lastSeenHM = this.loadLastSeen();
	}
	public void setMutelist(List<String> muteList) {
		this.muteList = muteList;
	}
	public ConfigReader getLastSeen() {
		return this.lastSeen;
	}
	
	public void setLastSave(Long lastSave) {
		this.lastSave = lastSave;
	}
	public Long getLastSave() {
		return this.lastSave;
	}
	
    public void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) {
    	if(sourceNick.equalsIgnoreCase("NickServ")) {
    		if(notice.contains("NickServ IDENTIFY")) {
    			if(this.instance.getPassword() != null) {
    				sendMessage("NickServ", "identify " + this.instance.getPassword());
    				for(String c :this.getChannels()) {
        				partChannel(c);
        				joinChannel(c);
    				}
    			}
    		}
    	}
    	System.out.println("[N]<"+sourceNick+">:" +notice);
    }
    
    public String trimMessage(String message) {
    	String msg = null;
    	if(message.substring(0,1).equalsIgnoreCase("!")) {
    		msg = message.substring(1);
    	} else if(message.length() > this.getNick().length() + 2) {
    		if(message.substring(0, this.getNick().length() + 2).equalsIgnoreCase(this.getNick() + ", ")) {
    			msg = message.substring(this.getNick().length() + 2);
    		} else if (message.substring(message.length() - this.getNick().length()).equalsIgnoreCase(this.getNick())) {
    			msg = message.substring(0, message.length() - this.getNick().length());
    		} else if(message.substring(0, this.getNick().length() + 1).matches(this.getNick() + " ")) {
    			msg = message.substring(this.getNick().length() + 1);
    		}
    	}
    	System.out.println(msg.trim());
    	return msg.trim();
    }
    
    public String getCommand(String message) {
    	String msg = this.trimMessage(message);
		String[] tParams = msg.split(" ");
		String command = tParams[0];
		return command;
    }
    
    public String[] getParams(String message) {
    	String msg = this.trimMessage(message);
		String[] tParams = msg.split(" ");
		String[] params = new String[tParams.length];
		Boolean first = true;
		Integer i = 0;
		for(String p : tParams) {
			if(first) {
				first = false;
			} else {
				first = false;
				params[i] = p;
				i++;
			}
		}
		return params;
    }
    
    public String broadSeen(String[] params, String channel) {
    	Boolean found = false;
    	String user = null;
		if(params.length > 0) {
			for(String param : params) {
				if(param == null)
					continue;
				System.out.println("Checking seen for " + param);
				if(this.lastSeenHM.containsKey(param.toLowerCase())) {
					System.out.println(param + " found");
					found = true;
					user = param;
				}
			}
		}
    	if(found){
			if(this.lastSeenHM.containsKey(user.toLowerCase())) {
				return(user + " last seen on " + this.lastSeenHM.get(user.toLowerCase()));
			} else {
				return("I don't know who "+user+" is");
			}
    	} else {
			return("I don't know who you're looking for");
    	}
    }
    public void onJoin(String channel, String sender, String login, String hostname) {    
    	if(!sender.equals(this.getNick())) {
    		if(this.banList.contains(sender)) {
    			kick(channel, sender, "Banned");
    			return;
    		}
	    	if(!this.muteList.contains(sender)) {
	        	voice(channel, sender);
	        	System.out.println(sender + " joined " + channel + ", voiced");
	    	} else {
	    		deVoice(channel, sender);
	        	System.out.println(sender + " joined " + channel + ", muted");
	    	}
    	}
    	
    	this.currentUsers = this.getUsers(channel);
    }
    
    public void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
    	System.out.println(sourceNick + " set " + targetNick + " " +mode);
    }
    
    public void onMessage(String channel, String sender, String login, String hostname, String message) {
    	Boolean isCommand = false;
    	Boolean softCommand = false;
    	Long currentTime = System.currentTimeMillis();
    	Date d = new Date();
    	d.setTime(currentTime);
    	this.lastSeenHM.put(sender.toLowerCase(), d);
    	if(currentTime - this.lastSave > (60)) {
    		this.saveLastSeen();
    		this.saveMuted();
    	}
    	if(message.substring(0,1).matches("!")) {
    		isCommand = true;
    	} else if(message.length() > getNick().length() + 1) {
    		if(message.substring(0, getNick().length() + 2).matches(getNick() + ", ")) {
    			isCommand = true;
    		} else if (message.substring(message.length() - this.getNick().length()).equalsIgnoreCase(this.getNick())) {
    			isCommand = true;
    			softCommand = true;
    		} else if(message.substring(0, this.getNick().length() + 1).matches(this.getNick() + " ")) {
        			isCommand = true;
    		}
    		System.out.println(">"+message.substring(message.length() - this.getNick().length())+"<");
    	}
    	if(this.muteList.contains(sender)) {
    		for(User u : this.currentUsers) {
    			if(!u.equals(sender))
    				continue;
    			
    			if(u.isOp()) {
    				deOp(channel, sender);
    			}
        		deVoice(channel, sender);
        		sendNotice(sender, "You've been muted, go talk in #stfu");
    		}
    	}
    	if(isCommand) {
    		String command = getCommand(message).trim().toLowerCase();

    		if(OBot.isDebug) {
    		System.out.println("1:"+command);
    		System.out.println("2:"+command.substring(command.length() - 1));
    		System.out.println("3:"+command.substring(0, command.length() - 1));
    		}
    		if(command.substring(command.length() - 1).equals(",")) {
    			command = command.substring(0, command.length() - 1);
    		}
    		String[] params = getParams(message);
            String time = new java.util.Date().toString();
            String currentDay = Calendar.getInstance().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH);
    		
            String[] secureCommands = {"voice", "devoice", "save", "mute", "op", "deop", "hop", "check"};
            for(String cm : secureCommands) {
            	if(command.equals(cm)) {
            		if(!this.instance.owners.contains(sender)) {
                		sendNotice(sender, "You don't have access to this command");
                		return;
            		}
            	}
            }
    		switch(command) {
    			case "hi":
    				sendMessage(channel, "Hey " + sender);
    				break;
    			case "hey":
    				sendMessage(channel, "Hey " + sender);
    				break;
    			case "sup":
    				sendMessage(channel, "Hey " + sender);
    				break;
    			case "time":
    				sendMessage(channel, "It is now ["+time+"]");
    				break;
    			case "yay":
    				sendMessage(channel, "〰😀〰");
    				break;
    			case "what":
    				if(message.contains("what") && message.contains("time")) {
        				sendMessage(channel, "It is now ["+time+"]");
    				}
    				if(message.contains("what") && message.contains("day")) {
        				sendMessage(channel, "It is now ["+currentDay+"]");
    				}
    				break;
    			case "ping":
					Long newTime = System.currentTimeMillis();
					Long difference = newTime - currentTime;
					sendNotice(sender, "Pong! - " + difference + "ms");
					break;
    			case "op":
    				String user = sender;
    				Boolean found = false;
    				if(params.length > 0) {
    					System.out.println("Param " + params[0]);
    					for(User u : getUsers(channel)) {
    						String nick = u.getNick().replace(u.getPrefix(), "");
        					System.out.println("User " + nick);
    						if(nick.equals(params[0]))
    							found = true;
    					}
    					if(found) {
    						user = params[0];
    						voice(channel, user);
    					}
    				}
    				break;
    			case "deop":
    				String user5 = sender;
    				Boolean found6 = false;
    				if(params.length > 0) {
    					System.out.println("Param " + params[0]);
    					for(User u : getUsers(channel)) {
    						String nick = u.getNick().replace(u.getPrefix(), "");
        					System.out.println("User " + nick);
    						if(nick.equals(params[0]))
    							found6 = true;
    					}
    					if(found6) {
    						user5 = params[0];
    						voice(channel, user5);
    					}
    				}
    				break;
    			case "seen":
    				if(params.length > 0) {
    					System.out.println("Checking seen for " + params[0]);
    					if(this.lastSeenHM.containsKey(params[0].toLowerCase())) {
        					System.out.println(params[0] + " not found");
        					Date last = this.lastSeenHM.get(params[0].toLowerCase());
        					Date now = new Date();
        			    	now.setTime(currentTime);
        			    	long lastMS = last.getTime();
        			    	long nowMS = now.getTime();
        			    	long diff = nowMS - lastMS;
        			    	
        			    	System.out.println("Difference in MS:" + diff);
        			    	
        			    	long diffSeconds = diff / 1000;
        			    	long diffMinutes = diffSeconds / 60;
        			    	long diffHours = diffMinutes / 60;
        			    	long diffDays = diffHours / 24;
        			    	
        			    	long secondsLeft = diffSeconds;

        			    	System.out.println("diffSeconds: " + diffSeconds);
        			    	System.out.println("diffMinutes: " + diffMinutes);
        			    	System.out.println("diffHours: " + diffHours);
        			    	System.out.println("diffDays: " + diffDays);
        			    	
        			    	int days = (int) diffDays;
        			    	System.out.println(days + " days");
        			    	secondsLeft -= days * 24 * 60 * 60;
        			    	
        			    	int hours = (int) ((secondsLeft / 60) / 60);
        			    	System.out.println(hours + " hours");
        			    	secondsLeft -= hours * 60 * 60;
        			    	
        			    	int minutes = (int) (secondsLeft / 60);
        			    	System.out.println(minutes + " minutes");
        			    	secondsLeft -= minutes * 60;
        			    	
        			    	int seconds = (int) secondsLeft;
        			    	System.out.println(seconds + " seconds");
        			    	
        			    	
        			    	
    						sendMessage(channel, params[0] + " was last seen "+ days + " days, " + hours + " hours and " + minutes + ":"+ seconds+ " minutes ago [" + this.lastSeenHM.get(params[0].toLowerCase()).toString() + "]");
    					} else {
    						sendMessage(channel, "I don't know who "+params[0]+" is");
    					}
    				}
    				break;
    			case "voice":
    				String user7 = sender;
    				Boolean found7 = false;
    				if(params.length > 0) {
    					System.out.println("Param " + params[0]);
    					for(User u : getUsers(channel)) {
    						String nick = u.getNick().replace(u.getPrefix(), "");
        					System.out.println("User " + nick);
    						if(nick.equals(params[0]))
    							found7 = true;
    					}
    					if(found7) {
    						user7 = params[0];
    	    				if(!this.muteList.contains(user7)) {
    	    					voice(channel, user7);
    	    				}
    					}
    				}
    				break;
    			case "devoice":
    				String user2 = sender;
    				Boolean found2 = false;
    				if(params.length > 0) {
    					for(User u : getUsers(channel)) {
    						String nick2 = u.getNick().replace(u.getPrefix(), "");
        					System.out.println("User " + nick2);
    						if(nick2.equals(params[0]))
    							found2 = true;
    					}
    					if(found2) {
    						user2 = params[0];
    						deVoice(channel, user2);
    					}
    				}
    				break;
    			case "when":
    				if(message.contains("last") && (message.contains("seen") || message.contains("saw") || message.contains("see"))) {
						sendMessage(channel, broadSeen(params,channel));
    				}
    				break;
    			case "save":
    				if(this.instance.owners.contains(sender)) {
	    				this.saveLastSeen();
	    				this.saveMuted();
	    				sendNotice(sender, "Data saved");
    				}
    				break;
    			case "load":
    				if(this.instance.owners.contains(sender)) {
    					this.lastSeenHM = this.loadLastSeen();
    					this.loadMuted();
    				}
    				break;
    			case "mute":
    				String user4 = sender;
    				Boolean found4 = false;
    				if(params.length > 0) {
    					System.out.println("Param " + params[0]);
    					for(User u : getUsers(channel)) {
    						String nick = u.getNick().replace(u.getPrefix(), "");
        					System.out.println("User " + nick);
    						if(nick.equals(params[0]))
    							found4 = true;
    					}
    					if(found4) {
    						user4 = params[0];
    						this.muteList.add(user4);
							deOp(channel, user4);
							deVoice(channel, user4);
    					}
    				}
    				break;
    			case "unmute":
    				if(params.length > 0) {
    					if(this.muteList.contains(params[0])) {
    						this.muteList.remove(params[0]);
    						this.voice(channel,params[0]);
    					}
    				}
    			case "voiceall":
    				this.currentUsers = this.getUsers(channel);
    				for(User u: this.currentUsers) {
    					System.out.println("Checking " + u.getNick().replace(u.getPrefix(), ""));
    					System.out.println("Checking " + u.getNick() + " > " + u.getPrefix());
    					if(!this.muteList.contains(u.getNick().replace(u.getPrefix(), ""))) {
    						if(!u.hasVoice()) {
    							if(!u.isOp()) {
    								voice(channel, u.getNick().replace(u.getPrefix(), ""));
    							}
    						}
    					}
    				}
    				break;
    			case "hop":
    				partChannel(channel);
    				joinChannel(channel);
    				break;
    			case "topic":
    				this.sendMessage(channel, "Current topic:" + this.topic);
    				break;
    			case "check":
    				this.APIUpdate(sender, channel);
    				break;
    			case "version":
    				sendNotice(sender, OBot.VERSION);
    				break;
    			case "u":
    				String dUser = sender;
    				if(params.length > 0 && params[0] != null) {
    					System.out.println("User changed from " + dUser + " to [" + params[0] + "]");
    					dUser = params[0];
    				}
    				this.getUserData(dUser, sender, channel);
    				break;
    			case "note":
    				handleNotes(params, sender, channel);
    				break;
    			case "ban":
    				if(params.length > 0 && params[0] != null) {
    					for(User u: this.currentUsers) {
    						String nick = u.getNick().replace(u.getPrefix(), "");
    						if(nick.equalsIgnoreCase(params[0])) {
    							if(!this.banList.contains(params[0])) {
    								this.banList.add(params[0]);
    								kick(channel, params[0], "Banned");
    								this.saveMuted();
    							}
    						}
    					}
    				}
    				break;
    			case "unban":
    				if(params.length > 0 && params[0] != null) {
    					for(User u: this.currentUsers) {
    						String nick = u.getNick().replace(u.getPrefix(), "");
    						if(nick.equalsIgnoreCase(params[0])) {
    							if(this.banList.contains(params[0])) {
    								this.banList.remove(params[0]);
    								this.saveMuted();
    							}
    						}
    					}
    				}
    				break;
    			case "convert":
    				if(params.length > 2) {
    					String in = params[0];
    					String out = params[1];
    					if(NumberUtils.isNumber(params[2])) {
    						Conversion conv = new Conversion();
    						sendMessage(channel, conv.convertUnits(in, out, Double.parseDouble(params[2])));
    					} else {
    						sendMessage(channel, params[2] + " is not a number");
    					}
    				}
    				break;
    				
    				
    			
    				
    			default:
    				if(!softCommand)
    					sendMessage(channel, "I don't know what you mean");
    				break;
    		} // End of switch
    		
    		// Command
    	} else {
    		// Not command

			if(message.contains("fuck you")) {
				sendMessage(channel, "Fuck you too " + sender);
			} else if(message.contains("fuck dieNal")){
				sendMessage(channel, "I know, right?");
			}
			if(message.contains("fuck")) {
				for(User u: this.currentUsers) {
					System.out.println("Checking " + u.getNick().replace(u.getPrefix(), ""));
					System.out.println("Checking " + u.getNick());
					if(message.contains("fuck " + u.getNick().replace(u.getPrefix(), ""))) {
						sendMessage(channel, "Hey that ain't very nice");
					}
				}
			}
    	}
    }
    
    public void handleNotes(String[] params, String sender, String channel) {
    	if(params[0] == null) {
    		sendNotice(sender, "!note <SEND <RCPT> <MSG>|READ <NUM>|LIST>");
    		return;
    	}
    	String cmd = params[0];
    	if(cmd.equalsIgnoreCase("SEND")) {
    		
    	}
    	if(cmd.equalsIgnoreCase("READ")) {
    		
    	}
    	if(cmd.equalsIgnoreCase("LIST")) {
    		
    	}
    }
    
    public void getUserData(String user, String sender, String channel) {
    	if(user.equals(sender) || 
    		this.instance.owners.contains(sender)) {
    		String nChannel = "#PassGames";
        	if(channel != null) {
        		nChannel = channel;
        	}
            HashMap<String, String> apiMap = new HashMap<String, String>();
            HttpResponse<String> apiRequest = null;
            Map<String, Object> reqMap = new HashMap<String, Object>();
            reqMap.put("action", "olivier");
            reqMap.put("opt", "user");
            reqMap.put("user", user);
            try
    		{
    			apiRequest = Unirest.get("https://pgt.passgamesto.me/ajax.php").queryString(reqMap).asString();
    			System.out.println(Unirest.get("https://pgt.passgamesto.me/ajax.php").queryString(reqMap).getUrl());
    		}catch (UnirestException e){e.printStackTrace();
    		}
            
            if(apiRequest != null) {
            	System.out.println(apiRequest.getBody());
            }
            if(apiRequest != null) {
            	JSONObject ar = new JSONObject(apiRequest.getBody());
            	if(ar.has("response")) {
            		JSONObject resp = ar.getJSONObject("response");
            		String[] names = JSONObject.getNames(resp);
            		for(String n : names) {
            			apiMap.put(n, resp.getString(n));
            		}
            	}
            }
            if(apiMap.containsKey("user")) {
        		String nMsg = this.colorFormat(apiMap.get("user"));
        		this.sendMessage(nChannel, nMsg);
            }
    	}
    }
    
    public void APIUpdate(String sender, String channel) {
    	String nChannel = "#PassGames";
    	if(channel != null) {
    		nChannel = channel;
    	}
        HashMap<String, String> apiMap = new HashMap<String, String>();
        HttpResponse<String> apiRequest = null;
        Map<String, Object> reqMap = new HashMap<String, Object>();
        reqMap.put("action", "olivier");
        reqMap.put("opt", "info");
        try
		{
			apiRequest = Unirest.get("https://pgt.passgamesto.me/ajax.php").queryString(reqMap).asString();
		}catch (UnirestException e){e.printStackTrace();
		}
        
        if(apiRequest != null) {
        	System.out.println(apiRequest.getBody());
        }
        if(apiRequest != null) {
        	JSONObject ar = new JSONObject(apiRequest.getBody());
        	if(ar.has("response")) {
        		JSONObject resp = ar.getJSONObject("response");
        		String[] names = JSONObject.getNames(resp);
        		for(String n : names) {
        			apiMap.put(n, resp.getString(n));
        		}
        	}
        }
        
        if(apiMap.containsKey("topic")) {
        	if(!apiMap.get("topic").equals(this.topic)) {
        		//this.setTopic("#PassGames", apiMap.get("topic"));
        		if(sender != null) {
            		//sendMessage(sender, "New topic found");
            		//sendMessage(sender, apiMap.get("topic"));
            		//sendMessage(sender, "Old one:");
            		//sendMessage(sender, this.topic);
            		String nMsg = this.colorFormat(apiMap.get("topic"));
            		//sendMessage(sender, "Test: " + nMsg);
            	    if(this.topic != nMsg) {
            	    	this.setTopic(nChannel, nMsg);
            	    }
        		}
        	}
        }
    }
    
    public String colorFormat(String str) {
	    String re1="(\\\\)";	// Any Single Character 1
	    LinkedHashMap<String, String> repArray = new LinkedHashMap<String, String>();
	    repArray.put("u000315", Colors.LIGHT_GRAY);
	    repArray.put("u000314", Colors.DARK_GRAY);
	    repArray.put("u000313", Colors.MAGENTA);
	    repArray.put("u000312", Colors.BLUE);
	    repArray.put("u000311", Colors.CYAN);
	    repArray.put("u000310", Colors.TEAL);
	    repArray.put("u00039", Colors.GREEN);
	    repArray.put("u00038", Colors.YELLOW);
	    repArray.put("u00037", Colors.OLIVE);
	    repArray.put("u00036", Colors.PURPLE);
	    repArray.put("u00035", Colors.BROWN);
	    repArray.put("u00034", Colors.RED);
	    repArray.put("u00033", Colors.DARK_GREEN);
	    repArray.put("u00032", Colors.DARK_BLUE);
	    repArray.put("u00031", Colors.BLACK);
	    repArray.put("u00030", Colors.WHITE);
	    repArray.put("u0002", Colors.BOLD);
	    repArray.put("u000f", Colors.NORMAL);
	    
	    String nStr = str;
	    
	    Iterator<Entry<String, String>> it = repArray.entrySet().iterator();
	    while(it.hasNext()) {
	    	Entry<String, String> e = it.next();
    	    String re2="("+e.getKey() + ")";	// Variable Name 1
    	    Pattern p = Pattern.compile(re1+re2,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	    	//System.out.println("Now replacing "+ p.toString());
    		nStr = nStr.replaceAll(p.toString(), e.getValue());
	    }
	    
	    return nStr;
    }
    
    public void onPrivateMessage(String sender, String login, String hostname, String message) {
    	if(sender.equals("iCurse")) {
    		//this.APIUpdate(sender, null);
    	}
    }
    
    public void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
    	if(channel.contains("PassGames")) {
	    	this.topic = topic;
	    	Date d = new Date(date);
    		System.out.println("");
	    	if(!changed) {
	    		System.out.println("Topic set by " + setBy + " on " + d.toString());
	    		System.out.println("Current topic:");
	    	} else {
	    		System.out.println("Topic changed by " + setBy + " on " + d.toString());
	    		System.out.println("New topic:");
	    	}
    		System.out.println(topic);
    	}
    	
    }
    
}
