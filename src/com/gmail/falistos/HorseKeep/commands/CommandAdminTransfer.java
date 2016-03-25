package com.gmail.falistos.HorseKeep.commands;

import com.gmail.falistos.HorseKeep.HorseKeep;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandAdminTransfer extends ConfigurableCommand {
	public CommandAdminTransfer(HorseKeep plugin, CommandSender sender, String[] args)
	{
		super(plugin, sender, args);
		
		if (args.length < 3)
		{
			sender.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("missingHorseIdentifier"));
			return;
		}
		
		String horseIdentifier = args[2];
		
		if (!plugin.manager.horseIdentifierExists(horseIdentifier))
		{
			sender.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("horseDoesntExists"));
			return;
		}
		
		if (args.length < 4)
		{
			sender.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("missingPlayer"));
			return;
		}
		
		String playerName = args[3];
	
		plugin.manager.setHorseOwner(playerName, plugin.manager.getHorseUUID(horseIdentifier));
		
		sender.sendMessage(this.getPrefix() + plugin.lang.get("transferedHorse").replace("%player", playerName).replace("%id", horseIdentifier));
	}
}