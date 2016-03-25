package com.gmail.falistos.HorseKeep.commands;

import com.gmail.falistos.HorseKeep.HorseKeep;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSummon extends ConfigurableCommand {
	public CommandSummon(HorseKeep plugin, CommandSender sender, String[] args)
	{
		super(plugin, sender, args);
		
		if (!(sender instanceof Player)) { sender.sendMessage(plugin.lang.get("canOnlyExecByPlayer")); return; }
		
		Player player = (Player) sender;
		
		if (!plugin.perm.has(player, "horsekeep.summon") && !plugin.perm.has(player, "horsekeep.admin"))
		{
			player.sendMessage(this.getPrefix() + ChatColor.RED + plugin.lang.get("noPermission"));
			return;
		}
		
		if (args.length < 2)
		{
			player.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("missingHorseIdentifier"));
			return;
		}
		
		String horseIdentifier = args[1];
		
		if (!plugin.manager.horseIdentifierExists(horseIdentifier))
		{
			player.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("horseDoesntExists"));
			return;
		}

		if (!plugin.manager.isHorseOwner(horseIdentifier, player.getUniqueId()) && !plugin.perm.has(player, "horsekeep.admin"))
		{
			player.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("dontOwnThisHorse"));
			return;
		}
		
		if (!plugin.manager.isStored(plugin.manager.getHorseUUID(horseIdentifier)))
		{
			player.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("horseCurrentlyNotStored"));
			return;
		}
		
		plugin.manager.summon(horseIdentifier, player.getLocation());
        
		player.sendMessage(this.getPrefix() + plugin.lang.get("horseSummoned"));
	}
}