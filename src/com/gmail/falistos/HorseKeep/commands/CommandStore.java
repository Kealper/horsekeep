package com.gmail.falistos.HorseKeep.commands;

import com.gmail.falistos.HorseKeep.HorseKeep;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.World;

import java.util.UUID;

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

		LivingEntity horse = null;
		boolean isMounted = false;

		if (plugin.manager.isOnHorse(player) && args.length < 2) {
			horse = (LivingEntity) player.getVehicle();
			isMounted = true;
		} else {
			if (args.length < 2) {
				player.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("missingHorseIdentifier"));
				return;
			}
			UUID horseUUID = plugin.manager.getHorseUUID(args[1]);

			if (plugin.manager.isStored(horseUUID)) {
				player.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("horseAlreadyStored"));
				return;
			}

			for(World w: this.plugin.getServer().getWorlds()) {
				for(LivingEntity e: w.getLivingEntities()) {

					if (horseUUID.toString().equalsIgnoreCase(e.getUniqueId().toString())) {
						if (!e.getLocation().getChunk().isLoaded()) {
							e.getLocation().getChunk().load();
						}

						horse = e;
						break;
					}
				}

				if (horse != null) {
					break;
				}
			}
		}

		if (horse == null) {
			sender.sendMessage(this.getPrefix() + ChatColor.GOLD + plugin.lang.get("horseDoesntExists"));
			return;
		}

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

		if (isMounted) {
			horse.eject();
		}

		plugin.manager.store(horse);

		horse.remove();

		sender.sendMessage(this.getPrefix() + plugin.lang.get("horseStored").replace("%id", plugin.manager.getHorseIdentifier(horse.getUniqueId())));
	}
}