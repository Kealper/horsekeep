package com.gmail.falistos.HorseKeep.commands;

import com.gmail.falistos.HorseKeep.HorseKeep;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSetIdentifier extends ConfigurableCommand {
	public CommandSetIdentifier(HorseKeep plugin, CommandSender sender, String[] args)
	{
		super(plugin, sender, args);
		
		if (!(sender instanceof Player)) { sender.sendMessage(plugin.lang.get("canOnlyExecByPlayer")); return; }
		
		Player player = (Player) sender;
		
		if (args.length < 2) { player.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("missingHorseIdentifier")); return; }
		
		String horseIdentifier = args[1];
		
		if (!plugin.manager.horseIdentifierExists(horseIdentifier)) { sender.sendMessage(plugin.prefix + ChatColor.GOLD + plugin.lang.get("horseDoesntExists")); return; }
		
		if (!plugin.manager.isHorseOwner(horseIdentifier, player.getUniqueId()) && !plugin.perm.has(sender, "horsekeep.admin"))
		{
			player.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("dontOwnThisHorse"));
			return;
		}
		
		if (args.length < 3) { 
			player.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("missingHorseNewIdentifier")); 
			return;
		}

		Boolean hasWhiteSpace = false;
		for (char c : args[2].toCharArray()) {
		    if (Character.isWhitespace(c)) {
		    	hasWhiteSpace = true;
		    }
		}
		
		if (hasWhiteSpace) { player.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("identifierNoWhitespaces")); return; }
		
		if (plugin.manager.isHorseIdentifierTaken(args[2])) { player.sendMessage(plugin.prefix + plugin.lang.get("identifierAlreadyTaken")); return; }
		
		plugin.data.getHorsesData().set("horses."+plugin.manager.getHorseUUID(args[1])+".identifier", args[2]);
		plugin.data.save();
		
		player.sendMessage(this.getPrefix() + plugin.lang.get("newIdentifierSet")+" "+args[2]);
	}
}