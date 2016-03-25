package com.gmail.falistos.HorseKeep;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class UUIDUtils {
	
	private UUIDUtils() { }
	
	@SuppressWarnings("deprecation")
	public static UUID getPlayerUUID(String playerName)
	{
		Player player = Bukkit.getPlayerExact(playerName);
    	
    	if (player != null) return player.getUniqueId();
    	else return Bukkit.getOfflinePlayer(playerName).getUniqueId();
	}
	
	public static String getPlayerName(UUID playerUUID)
	{
		Player player = Bukkit.getPlayer(playerUUID);
    	
    	if (player != null) return player.getName();
    	else return Bukkit.getOfflinePlayer(playerUUID).getName();
	}
	
	public static boolean compareUUID(UUID a, UUID b)
	{
		if (a.toString().equals(b.toString())) return true;
		return false;
	}
	
}
