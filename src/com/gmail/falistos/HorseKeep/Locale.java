package com.gmail.falistos.HorseKeep;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;

public class Locale {
	
	private FileConfiguration lang = null;
	private File langFile = null;
	
	private Plugin plugin;
	
	private String filePath;
	private String defaultFile;
	
	public Locale(Plugin plugin, String filePath, String defaultFile)
	{
		this.plugin = plugin;
		
		InputStream fileStream = plugin.getResource(filePath);
		
		this.defaultFile = defaultFile;
		
		if (fileStream != null) {
			this.filePath = filePath;
		}
		else this.filePath = this.defaultFile;
		
		this.reload();
	}
	
	public void reload() {
	    if (langFile == null) {
	    	langFile = new File(plugin.getDataFolder(), filePath);
	    }
	    lang = YamlConfiguration.loadConfiguration(langFile);

	    // Set defaults
	    InputStream fileStream = plugin.getResource(defaultFile);
	    if (fileStream != null) {
	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(fileStream);
	        lang.setDefaults(defConfig);
	    }
	}
	
	public FileConfiguration getConfiguration() {
	    if (lang == null) {
	        this.reload();
	    }
	    return lang;
	}
	
	public void saveDefault() {
	    if (langFile == null) {
	        langFile = new File(plugin.getDataFolder(), filePath);
	    }
	    if (!langFile.exists()) {            
	        plugin.saveResource(defaultFile, false);
	    }
	}
	
	public String get(String key)
	{
		String value = this.getConfiguration().getString(key);
		
		if (value != null)
		{
			return value;
		}
		else 
		{
			plugin.getLogger().severe("Missing locale entry for variable "+key);
			return null;
		}
	}

}
