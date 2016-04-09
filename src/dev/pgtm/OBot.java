package dev.pgtm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;

public class OBot
{
    private Olivier bot;
    private String name;
    private String password;
    private String server;
    private Integer port;
    private String server_password;
    private ConfigReader cR;
    private ConfigReader lastSeen;
    ArrayList<String> owners;
    ArrayList<String> muted;
    public static final String VERSION = PircBot._version + " by iCurse";
    public static final Boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().
    	    getInputArguments().toString().indexOf("jdwp") >= 0;
    
    public OBot() throws IOException, NickAlreadyInUseException, IrcException {
        this.bot = new Olivier(this);
        this.bot.setVerbose(true);
        this.cR = new ConfigReader();
        this.bot.setConfigReader(cR);
        this.lastSeen = new ConfigReader("lastseen.log");
        this.bot.setLastSeen(lastSeen);
        this.bot.setLastSave(new Date().getTime());
        this.bot.loadMuted();
        String name = "Olivier";
        @SuppressWarnings("unused")
		String password = null;
        
        this.server = cR.loadProperty("server");
        if(cR.hasProperty("port"))
            port = Integer.parseInt(cR.loadProperty("port"));
        if(cR.hasProperty("server_password"))
            server_password = cR.loadProperty("server_password");
        if(cR.hasProperty("nick"))
            name = cR.loadProperty("nick");
        if(cR.hasProperty("password"))
            password = cR.loadProperty("password");
        
        if(port != null) {
            if(server_password != null) {
                this.bot.connect(server, port, server_password);
            } else {
                this.bot.connect(server, port);
            }
        } else {
            this.bot.connect(server);
        }
        
        this.bot.changeNick(name);
        
        String channel = cR.loadProperty("channels");
        if(channel.contains(",")) {
        	String[] channels = channel.split(",");
        	for(Integer i = 0; i < channels.length; i++) {
                this.bot.joinChannel(channels[i]);
        	}
        } else {
            this.bot.joinChannel(channel);
        }

        String owner = cR.loadProperty("owners");
        this.owners = new ArrayList<String>();
        if(owner.contains(",")) {
        	String[] owners = owner.split(",");
        	for(String o : owners) {
        		this.owners.add(o);
        	}
        } else {
    		this.owners.add(owner);
        }
        String muted = cR.loadProperty("muted");
        this.muted = new ArrayList<String>();
        if(muted.contains(",")) {
        	String[] muteds = muted.split(",");
        	for(String o : muteds) {
        		this.muted.add(o);
        	}
        } else {
    		this.muted.add(owner);
        }
    }
    
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
        this.bot.changeNick(name);
    }
    
    public String getPassword() {
        return this.password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getServer() {
        return this.server;
    }
    public void setServer(String server) {
        this.server = server;
    }
    
    public Integer getPort() {
        return this.port;
    }
    public void setPort(Integer port) {
        this.port = port;
    }
    
    public String getServerPassword() {
        return this.server_password;
    }
    public void setServerPassword(String server_password) {
        this.server_password = server_password;
    }
    
    public Olivier getOlivier() {
    	return this.bot;
    }
}
