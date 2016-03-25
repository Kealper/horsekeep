package com.gmail.falistos.HorseKeep.commands;

import com.gmail.falistos.HorseKeep.HorseKeep;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandUnprotect extends ConfigurableCommand {
	public CommandUnprotect(HorseKeep plugin, CommandSender sender, String[] args)
	{
		super(plugin, sender, args);
		
		if (!(sender instanceof Player)) { sender.sendMessage(plugin.lang.get("canOnlyExecByPlayer")); return; }
		
		Player player = (Player) sender;
		
		if (args.length < 2)
		{
			sender.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("missingHorseIdentifier"));
			return;
		}
		
		String horseIdentifier = args[1];
		
		if (!plugin.manager.horseIdentifierExists(horseIdentifier))
		{
			sender.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("horseDoesntExists"));
			return;
		}

		if (!plugin.manager.isHorseOwner(horseIdentifier, player.getUniqueId()) && !plugin.perm.has(sender, "horsekeep.admin"))
		{
			sender.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("dontOwnThisHorse"));
			return;
		}
		
		plugin.manager.removeHorse(horseIdentifier);
        
		sender.sendMessage(this.getPrefix() + plugin.lang.get("horseUnprotected"));
	}
}