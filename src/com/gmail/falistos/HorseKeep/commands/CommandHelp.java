package com.gmail.falistos.HorseKeep.commands;

import com.gmail.falistos.HorseKeep.HorseKeep;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandHelp {
	public CommandHelp(HorseKeep plugin, CommandSender sender, String[] args)
	{
		sender.sendMessage("=== "+ChatColor.GOLD+"["+ChatColor.GREEN+plugin.lang.get("commandsList")+ChatColor.GOLD+"] "+ChatColor.RESET+"===");
		sender.sendMessage("- "+ChatColor.AQUA+"/horse list|l");
		sender.sendMessage("- "+ChatColor.AQUA+"/horse tp <identifier>");
		sender.sendMessage("- "+ChatColor.AQUA+"/horse id|setid <identifier> <new-identifier>");
		sender.sendMessage("- "+ChatColor.AQUA+"/horse members|m <identifier>");
		sender.sendMessage("- "+ChatColor.AQUA+"/horse addmember|addm <identifier> <player>");
		sender.sendMessage("- "+ChatColor.AQUA+"/horse delmember|delm <identifier> <player>");
		sender.sendMessage("- "+ChatColor.AQUA+"/horse store");
		sender.sendMessage("- "+ChatColor.AQUA+"/horse summon <identifier>");
		sender.sendMessage("- "+ChatColor.AQUA+"/horse unprotect|up <identifier>");
		if (plugin.perm.has(sender, "horsekeep.admin")) {
			sender.sendMessage("- "+ChatColor.RED+"/horse tpall");
			sender.sendMessage("- "+ChatColor.RED+"/horse reload");
			sender.sendMessage("- "+ChatColor.RED+"/horse admin list");
			sender.sendMessage("- "+ChatColor.RED+"/horse admin transfer <identifier> <player>");
		}
	}
}