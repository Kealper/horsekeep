package com.gmail.falistos.HorseKeep.commands;

import com.gmail.falistos.HorseKeep.HorseKeep;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandReload extends ConfigurableCommand {
	public CommandReload(HorseKeep plugin, CommandSender sender, String[] args)
	{
		super(plugin, sender, args);
		
		if (sender instanceof Player)
		{
			if (!plugin.perm.has(sender, "horsekeep.admin"))
			{
				sender.sendMessage(this.getPrefix() + ChatColor.RED + plugin.lang.get("noPermission"));
				return;
			}
		}
		
		plugin.reloadConfig();
		plugin.lang.reload();
		plugin.data.reload();
		
		sender.sendMessage(this.getPrefix() + "HorseKeep reloaded");
	}
}