package dev.pgtm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

public class ConfigReader
{
	File config;
	Properties prop = new Properties();
	FileOutputStream output;
	FileInputStream input;
	String filename;
	
	public ConfigReader() throws IOException {
		this.filename = "config.properties";
		Init();
	}
	
	public ConfigReader(String fileName) throws IOException {
		this.filename = fileName;
		Init();
	}
	
	public void Init() throws IOException {
		this.config = new File(this.filename);
		if(!config.exists()) {
		    config.createNewFile();
		} 
		this.input = new FileInputStream(this.config);
		System.out.println(config.getPath());
		this.prop.load(this.input);
	}
	
	public List<String> getKeys() {
		List<String> list = new ArrayList<String>();
		for(Entry<Object, Object> e: this.prop.entrySet()){
			String key = (String) e.getKey();
			list.add(key);
		}
		return list;
	}

	String readFile() {
		  byte[] encoded = null;
		try
		{
			encoded = Files.readAllBytes(config.toPath());
			System.out.println("Read file");
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.out.println("Failed to read file");
		}
		  return new String(encoded, Charset.defaultCharset());
	}
	
	public boolean hasProperty(String key) {
		Enumeration<?> e = this.prop.propertyNames();
		Boolean found = false;
		while(e.hasMoreElements()){
			String elem = (String) e.nextElement();
			if(elem.equals(key)) {
				found = true;
			}
		}
		if(!found) {
			System.out.println("Key "+key+" not found, creating");
			this.writeProperty(key, "unset");
		} else {
			if(this.prop.getProperty(key).equalsIgnoreCase("unset") || this.prop.getProperty(key).equals("")){
				found = false;
			}
		}
		
		return found;
	}
	
	public void writeProperty(String key, String value) {
		this.prop.setProperty(key, value);
		if(OBot.isDebug) {
			System.out.println("Setting property " + key + " to " + value);
		}
		this.writeObject();
	}
	
	public String loadProperty(String key) {
		if(hasProperty(key)) {
			if(OBot.isDebug) {
				System.out.println("Property "+key+" exists");
			}
			return this.prop.getProperty(key);
		} else {
			if(OBot.isDebug) {
				System.out.println("Property "+key+" does not exists");
			}
			this.prop.setProperty(key, "Unset");
			this.writeObject();
			return "Unset";
		}
	}
	
	public void writeObject() {
		try
		{
			this.prop.store(new FileOutputStream(this.filename), null);
			if(OBot.isDebug) { System.out.println("File saving done"); }
		}
		catch (IOException e)
		{
			e.printStackTrace();
			if(OBot.isDebug) { System.out.println("File saving failed"); }
		}
		/*FileOutputStream outputstream = null;
		try
		{
			outputstream = new FileOutputStream(this.config);
			this.prop.store(outputstream, null);
			System.out.println("File saving done");
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.out.println("File saving failed");
		}*/
	}
	
	public String getPath() {
		return this.config.getAbsolutePath();
	}
	
	public String[] splitProperty(String property) {
		return property.split("|");
	}
}