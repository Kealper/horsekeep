package com.gmail.falistos.HorseKeep;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;

public class HorseData {

	private String horsesDataFilePath = null;
	private FileConfiguration horsesData = null;
	private File horsesDataFile = null;
	
	private Plugin plugin;
	
	public HorseData(Plugin plugin, String path)
	{
		this.horsesDataFilePath = path;
		this.plugin = plugin;
		this.reload();
	}
	
	// Migration for < 0.2.2 versions
	public void migrate()
	{
		if (plugin.getConfig().get("horses") != null && getHorsesData().get("horses") == null)
		{
			ConfigurationSection horses = plugin.getConfig().getConfigurationSection("horses");

			getHorsesData().set("horses", horses);
			plugin.getConfig().set("horses", null);

			save();
			plugin.saveConfig();
			
			plugin.getLogger().info("Migrated horse data to horses.yml successfully");
		}
	}
	
	// Migration for player UUID
	
	@SuppressWarnings("deprecation")
	public void migrateUUID()
	{
		ConfigurationSection horsesSection = this.getHorsesData().getConfigurationSection("horses");
		HashMap<String, String> players = new HashMap<String, String>();
		
		if (horsesSection == null) { return; }
		
		for(String key : horsesSection.getKeys(false))
		{
			if (horsesSection.isSet(key+".owner"))
			{
				if (horsesSection.get(key+".ownerUUID") == null)
				players.put(key, horsesSection.getString(key+".owner"));
			}
		}
		
		for(Entry<String, String> entry : players.entrySet())
		{
			String horseUUID = entry.getKey();
			String playerName = entry.getValue();
			
			OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(playerName);

			this.getHorsesData().set("horses."+horseUUID+".ownerUUID", player.getUniqueId().toString());
			this.getHorsesData().set("horses."+horseUUID+".owner", null);
			
			if (this.getHorsesData().isSet("horses."+horseUUID+".members"))
			{
				List<String> horsesList = this.getHorsesData().getStringList("horses."+horseUUID+".members");
				
				int i = 0;
				
		        for (String memberName : horsesList) {
		        	OfflinePlayer member = Bukkit.getServer().getOfflinePlayer(memberName);
		        	
		        	horsesList.set(i, member.getUniqueId().toString());
		        	i++;
		        }
		        
		        this.getHorsesData().set("horses."+horseUUID+".members", horsesList);
			}

		}
		
		this.save();
		
		plugin.getLogger().info(players.size()+" players successfully updated to UUID format");
	}
	
	public void reload() {
	    if (horsesDataFile == null) {
	    	horsesDataFile = new File(plugin.getDataFolder(), horsesDataFilePath);
	    }
	    horsesData = YamlConfiguration.loadConfiguration(horsesDataFile);

	    InputStream defConfigStream = plugin.getResource(horsesDataFilePath);
	    if (defConfigStream != null) {
	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
	        horsesData.setDefaults(defConfig);
	    }
	    
	    this.save();
	}
	
	public FileConfiguration getHorsesData() {
	    if (horsesData == null) {
	        this.reload();
	    }
	    return horsesData;
	}
	
	public void save() {
	    if (horsesData == null || horsesDataFile == null) {
	    	return;
	    }
	    try {
	        getHorsesData().save(horsesDataFile);
	    } catch (IOException ex) {
	    	plugin.getLogger().log(Level.SEVERE, "Could not save horse data to " + horsesDataFile, ex);
	    }
	}
	
}
