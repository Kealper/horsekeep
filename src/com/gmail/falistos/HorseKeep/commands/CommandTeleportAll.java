package com.gmail.falistos.HorseKeep.commands;

import com.gmail.falistos.HorseKeep.HorseKeep;
import com.gmail.falistos.HorseKeep.HorseTeleportResponse;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class CommandTeleportAll extends ConfigurableCommand {
	public CommandTeleportAll(HorseKeep plugin, CommandSender sender, String[] args)
	{
		super(plugin, sender, args);
		
		if (!(sender instanceof Player)) { sender.sendMessage(plugin.lang.get("canOnlyExecByPlayer")); return; }
		
		Player player = (Player) sender;
		
		if (!plugin.perm.has(player, "horsekeep.tp.all") && !plugin.perm.has(sender, "horsekeep.admin"))
		{
			super.sendMessage("noPermission", sender);
			return;
		}
		
		List<String> horsesList = plugin.manager.getOwnedHorses(player.getUniqueId());
		
		if (horsesList.size() == 0)
		{
			super.sendMessage("dontOwnHorses", sender);
			return;
		}
		
        for (String horseUUID : horsesList) {
			
        	HorseTeleportResponse response = plugin.manager.teleportHorse(UUID.fromString(horseUUID), player.getLocation());
			
        	String horseIdentifier = plugin.manager.getHorseIdentifier(UUID.fromString(horseUUID));
        	
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
        }
        
        player.sendMessage(this.getPrefix() + "Done");
	}
}