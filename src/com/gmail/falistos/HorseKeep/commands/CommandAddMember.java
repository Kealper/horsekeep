package com.gmail.falistos.HorseKeep.commands;

import com.gmail.falistos.HorseKeep.HorseKeep;
import com.gmail.falistos.HorseKeep.UUIDUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandAddMember extends ConfigurableCommand {
	public CommandAddMember(HorseKeep plugin, CommandSender sender, String[] args)
	{
		super(plugin, sender, args);
		
		if (!(sender instanceof Player)) { sender.sendMessage(plugin.lang.get("canOnlyExecByPlayer")); return; }
		
		Player player = (Player) sender;
		
		if (!plugin.perm.has(sender, "horsekeep.member.add") && !plugin.perm.has(sender, "horsekeep.admin"))
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

		if (!plugin.manager.isHorseOwner(horseIdentifier, player.getUniqueId()) && !plugin.perm.has(sender, "horsekeep.admin"))
		{
			sender.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("dontOwnThisHorse"));
			return;
		}
		
		if (args.length < 3)
		{
			sender.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("missingPlayer"));
			return;
		}
		
		String memberName = args[2];

		if (plugin.manager.isHorseMember(horseIdentifier, UUIDUtils.getPlayerUUID(memberName)))
		{
			sender.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("playerIsAlreadyMember").replace("%player", memberName));
			return;
		}
		
		plugin.manager.addHorseMember(horseIdentifier, UUIDUtils.getPlayerUUID(memberName));
		
		sender.sendMessage(this.getPrefix() + plugin.lang.get("addedMemberToHorse").replace("%player", memberName).replace("%id", horseIdentifier));
	}
}