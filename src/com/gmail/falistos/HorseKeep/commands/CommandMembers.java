package com.gmail.falistos.HorseKeep.commands;

import com.gmail.falistos.HorseKeep.HorseKeep;
import com.gmail.falistos.HorseKeep.UUIDUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class CommandMembers extends ConfigurableCommand {
	public CommandMembers(HorseKeep plugin, CommandSender sender, String[] args)
	{
		super(plugin, sender, args);
		
		if (!plugin.perm.has(sender, "horsekeep.member.list") && !plugin.perm.has(sender, "horsekeep.admin"))
		{
			sender.sendMessage(this.getPrefix() + ChatColor.RED + plugin.lang.get("noPermission"));
			return;
		}
		
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
		
		if (sender instanceof Player)
		{
			Player player = (Player) sender;
			
			if (!plugin.manager.isHorseOwner(horseIdentifier, player.getUniqueId()) && !plugin.perm.has(sender, "horsekeep.admin"))
			{
				sender.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("dontOwnThisHorse"));
				return;
			}
		}

		sender.sendMessage("=== "+ChatColor.GOLD+"["+ChatColor.GREEN+plugin.lang.get("horseMembers")+" ("+horseIdentifier+")"+ChatColor.GOLD+"] "+ChatColor.RESET+"===");
		
		List<String> horsesList = plugin.manager.getHorseMembers(plugin.manager.getHorseUUID(horseIdentifier));
		
        for (String memberUUID : horsesList) {
        	String memberName = UUIDUtils.getPlayerName(UUID.fromString(memberUUID));
        	if (memberName == null) { memberName = "Unknown"; }
        	sender.sendMessage("- "+ChatColor.AQUA+memberName);
        }
	}
}