package com.gmail.falistos.HorseKeep.commands;

import com.gmail.falistos.HorseKeep.HorseKeep;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

public class CommandAdminList extends ConfigurableCommand {
	public CommandAdminList(HorseKeep plugin, CommandSender sender, String[] args)
	{
		super(plugin, sender, args);
		
		if (args.length < 3)
		{
			sender.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("missingPlayer"));
			return;
		}
		
		String playerName = args[2];
		
		sender.sendMessage("=== "+ChatColor.GOLD+"["+ChatColor.GREEN+plugin.lang.get("playerHorses").replace("%player", playerName)+ChatColor.GOLD+"] "+ChatColor.RESET+"===");
		
		List<String> horsesList = plugin.manager.getOwnedHorses(playerName);

		String stored;
        for (String horseId : horsesList) {
        	if (plugin.manager.isStored(UUID.fromString(horseId))) { stored = ChatColor.RED+" ["+plugin.lang.get("stored")+"]"; }
        	else stored = "";
        	
        	sender.sendMessage("- "+plugin.lang.get("identifier")+": "+ChatColor.AQUA+plugin.manager.getHorseIdentifier(UUID.fromString(horseId))+stored);
        }
	}
}