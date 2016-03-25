package com.gmail.falistos.HorseKeep.commands;

import com.gmail.falistos.HorseKeep.HorseKeep;
import com.gmail.falistos.HorseKeep.HorseTeleportResponse;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CommandTeleport extends ConfigurableCommand {
	public CommandTeleport(HorseKeep plugin, CommandSender sender, String[] args)
	{
		super(plugin, sender, args);
		
		if (!(sender instanceof Player)) { sender.sendMessage(plugin.lang.get("canOnlyExecByPlayer")); return; }
		
		Player player = (Player) sender;
		
		if (!plugin.perm.has(player, "horsekeep.tp") && !plugin.perm.has(sender, "horsekeep.admin")) { player.sendMessage(this.getPrefix() + ChatColor.RED + plugin.lang.get("noPermission")); return; }
		
		if (args.length < 2) { player.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("missingHorseIdentifier")); return; }
		
		String horseIdentifier = args[1];
		
		if (!plugin.manager.horseIdentifierExists(horseIdentifier))
		{
			player.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("horseDoesntExists"));
			return;
		}

		if (!plugin.manager.isHorseOwner(horseIdentifier, player.getUniqueId()) && !plugin.perm.has(player, "horsekeep.admin"))
		{
			player.sendMessage(this.getPrefix()+ ChatColor.GOLD + plugin.lang.get("dontOwnHorse"));
			return;
		}
		
		UUID horseUUID = plugin.manager.getHorseUUID(horseIdentifier);
		
    	HorseTeleportResponse response = plugin.manager.teleportHorse(horseUUID, player.getLocation());
		
		if (response.equals(HorseTeleportResponse.NOT_TELEPORTED_ENTITY_DELETED)) { 
			player.sendMessage(this.getPrefix() + ChatColor.RED+plugin.lang.get("notTeleportedDeleted").replace("%id", horseIdentifier)); 
			plugin.manager.removeHorse(horseIdentifier); 
		}
		else if (response.equals(HorseTeleportResponse.NOT_TELEPORTED_WRONG_WORLD)) { 
			player.sendMessage(this.getPrefix() + ChatColor.GOLD+plugin.lang.get("notTeleportedWrongWorld").replace("%id", horseIdentifier)); 
		}
		else if (response.equals(HorseTeleportResponse.NOT_TELEPORTED)) { 
			player.sendMessage(this.getPrefix() + ChatColor.GOLD+plugin.lang.get("notTeleportedUnknown").replace("%id", horseIdentifier)); 
		}
		else if (response.equals(HorseTeleportResponse.NOT_TELEPORTED_STORED)) { 
			player.sendMessage(this.getPrefix() + ChatColor.GOLD+plugin.lang.get("notTeleportedStored").replace("%id", horseIdentifier)); 
		}
		
        player.sendMessage(this.getPrefix()+"Done");
	}
}