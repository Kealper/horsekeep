package com.gmail.falistos.HorseKeep.commands;

import com.gmail.falistos.HorseKeep.HorseKeep;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

public class CommandGetIdentifier extends ConfigurableCommand {
	public CommandGetIdentifier(HorseKeep plugin, CommandSender sender, String[] args)
	{
		super(plugin, sender, args);
		
		if (!(sender instanceof Player)) { sender.sendMessage(plugin.lang.get("canOnlyExecByPlayer")); return; }
		
		Player player = (Player) sender;
		
		if (!plugin.manager.isOnHorse(player))
		{
			player.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("mustRidingHorse"));
			return;
		}
		
		Horse horse = (Horse) player.getVehicle();
		
		if (!plugin.manager.isOwned(horse.getUniqueId()))
		{
			player.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("horseNotProtected"));
			return;
		}

		if (!plugin.manager.isHorseOwner(player.getUniqueId(), horse) && !plugin.perm.has(sender, "horsekeep.admin"))
		{
			player.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("dontOwnThisHorse"));
			return;
		}
		
		player.sendMessage(this.getPrefix() + plugin.lang.get("getHorseIdentifier").replace("%id", plugin.manager.getHorseIdentifier(horse.getUniqueId())));
	}
}