package com.gmail.falistos.HorseKeep.commands;

import com.gmail.falistos.HorseKeep.HorseKeep;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class CommandStore extends ConfigurableCommand {
	public CommandStore(HorseKeep plugin, CommandSender sender, String[] args)
	{
		super(plugin, sender, args);

		if (!(sender instanceof Player)) { sender.sendMessage(plugin.lang.get("canOnlyExecByPlayer")); return; }

		Player player = (Player) sender;

		if (!plugin.perm.has(sender, "horsekeep.store") && !plugin.perm.has(sender, "horsekeep.admin"))
		{
			sender.sendMessage(this.getPrefix() + ChatColor.RED + plugin.lang.get("noPermission"));
			return;
		}

		if (!plugin.manager.isOnHorse(player))
		{
			sender.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("mustRidingHorse"));
			return;
		}

		LivingEntity horse = (LivingEntity) player.getVehicle();

		if (!plugin.manager.isOwned(horse.getUniqueId()))
		{
			sender.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("horseNotProtected"));
			return;
		}

		if (!plugin.manager.isHorseOwner(player.getUniqueId(), horse) && !plugin.perm.has(sender, "horsekeep.admin"))
		{
			sender.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("dontOwnThisHorse"));
			return;
		}

		horse.eject();

		plugin.manager.store(horse);

		horse.remove();

		sender.sendMessage(this.getPrefix() + plugin.lang.get("horseStored").replace("%id", plugin.manager.getHorseIdentifier(horse.getUniqueId())));
	}
}