package com.gmail.falistos.HorseKeep.commands;

import com.gmail.falistos.HorseKeep.HorseKeep;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ConfigurableCommand {
	HorseKeep plugin = null;
	String prefix = null;
	CommandSender sender = null;
	
	public ConfigurableCommand(HorseKeep plugin, CommandSender sender, String[] args)
	{
		this.plugin = plugin;
	}
	
	public void sendMessage(String message, CommandSender sender)
	{
		sender.sendMessage(this.getPrefix() + this.plugin.lang.get(message));
	}
	
	public void setPrefix(String prefix)
	{
		this.prefix = prefix;
	}
	
	public String getPrefix()
	{
		return this.plugin.getChatPrefix();
	}
	
	public Player getPlayer()
	{
		if (this.sender instanceof Player)
		{
			return (Player) this.sender;
		}
		return null;
	}
}
