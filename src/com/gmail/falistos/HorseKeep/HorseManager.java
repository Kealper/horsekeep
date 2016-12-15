package com.gmail.falistos.HorseKeep;

import org.bukkit.*;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * HorseKeep API
 *
 * @author Falistos/BritaniaCraft
 * @version 0.3.6
 */

public class HorseManager {

	private Configuration config;
	private HorseKeep plugin;
	private HorseData data;
	private boolean shouldFlushData;

	private EntityType[] horseEntityTypes = {EntityType.HORSE, EntityType.DONKEY, EntityType.MULE, EntityType.ZOMBIE_HORSE, EntityType.SKELETON_HORSE, EntityType.LLAMA};

	public static HorseManager instance;

	public static HorseManager getInstance() {
        if (null == instance) {
            instance = new HorseManager(instance.plugin);
        }
        return instance;
    }

	public HorseManager(HorseKeep plugin)
	{
		this.plugin = plugin;
		this.config = plugin.getConfig();
		this.data = plugin.getHorseData();
		this.shouldFlushData = false;
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				if (shouldFlushData) {
					data.save();
					shouldFlushData = false;
				}
			}
		}, 20, 1200); // 1200 ticks = 60 seconds
	}

    public boolean isHorse(Entity entity) {
    	return isHorse(entity.getType());
    }

	public boolean isHorse(EntityType type) {
		for (EntityType horseType: horseEntityTypes) {
    		if (type == horseType) {
    			return true;
    		}
    	}

    	return false;
	}

    public boolean isOwned(UUID uuid)
    {
    	if (this.data.getHorsesData().isConfigurationSection("horses."+uuid)) return true;

		return false;
    }

    public boolean horseIdentifierExists(String horseIdentifier)
    {
    	if (!this.data.getHorsesData().isConfigurationSection("horses")) return false;

		ConfigurationSection horsesSection = this.data.getHorsesData().getConfigurationSection("horses");

		for(String key : horsesSection.getKeys(false)){
			if (this.data.getHorsesData().isSet("horses."+key+".identifier"))
			{
				if (this.data.getHorsesData().getString("horses."+key+".identifier").equalsIgnoreCase(horseIdentifier))
				{
					return true;
				}
			}
			else
			{
				this.plugin.getLogger().warning("Horse has no identifier set (UUID "+key+") - Auto-repair");
				this.data.getHorsesData().set("horses."+key+".identifier", this.getNewHorseIdentifier());
				this.data.save();
			}
		}

		return false;
    }

    public String getOwnerName(UUID horseUUID)
    {
    	UUID ownerUUID = this.getOwner(horseUUID);
    	String ownerName = UUIDUtils.getPlayerName(ownerUUID);

    	return ownerName;
    }

    public UUID getOwner(String horseIdentifier)
    {
    	return this.getOwner(this.getHorseUUID(horseIdentifier));
    }

    public UUID getOwner(UUID horseUUID)
    {
    	return UUID.fromString(this.data.getHorsesData().getString("horses."+horseUUID+".ownerUUID"));
    }

    public List<String> getHorseMembers(Entity horse)
    {
    	return this.getHorseMembers(horse.getUniqueId());
    }

    public List<String> getHorseMembers(UUID horseUUID)
    {
    	return this.data.getHorsesData().getStringList("horses."+horseUUID.toString()+".members");
    }

    public boolean canMountHorse(Player player, Entity horse)
    {
    	return this.canMountHorse(player, horse.getUniqueId());
    }

    public boolean canMountHorse(Player player, UUID horseUUID)
    {
    	String horseIdentifier = this.getHorseIdentifier(horseUUID);

    	if (this.isHorseMember(horseIdentifier, player.getUniqueId()) || this.isHorseOwner(horseIdentifier, player.getUniqueId()))
    	{
    		return true;
    	}

    	return false;
    }

    public void addHorseMember(String horseIdentifier, UUID playerUUID)
    {
    	List<String> horseMembers = this.getHorseMembers(this.getHorseUUID(horseIdentifier));

    	horseMembers.add(playerUUID.toString());

    	this.data.getHorsesData().set("horses."+getHorseUUID(horseIdentifier)+".members", horseMembers);

    	this.data.save();
    }

    public void removeHorseMember(String horseIdentifier, UUID playerUUID)
    {
    	List<String> horseMembers = this.getHorseMembers(this.getHorseUUID(horseIdentifier));

    	horseMembers.remove(playerUUID.toString());

    	this.data.getHorsesData().set("horses."+this.getHorseUUID(horseIdentifier)+".members", horseMembers);

    	this.data.save();
    }

    public boolean isHorseMember(String horseIdentifier, UUID playerUUID)
    {
    	List<String> horseMembers = this.getHorseMembers(this.getHorseUUID(horseIdentifier));

    	return horseMembers.contains(playerUUID.toString());
    }

    public boolean isHorseOwner(UUID playerUUID, Entity horse)
    {
    	if (UUIDUtils.compareUUID(this.getOwner(horse.getUniqueId()), playerUUID)) return true;
    	return false;
    }

    public boolean isHorseOwner(String horseIdentifier, UUID playerUUID)
    {
    	if (UUIDUtils.compareUUID(this.getOwner(horseIdentifier), playerUUID)) return true;
    	return false;
    }

	public void removeHorse(String horseIdentifier)
	{
		this.removeHorse(this.getHorseUUID(horseIdentifier));
	}

	public void removeHorse(UUID horseUUID)
	{
		this.data.getHorsesData().getConfigurationSection("horses").set(horseUUID.toString(), null);

		this.data.save();
	}

	public Integer getNewHorseIdentifier()
	{
		Integer identifierIncremental = this.config.getInt("internalIncrementalIdentifier");

		this.config.set("internalIncrementalIdentifier", (identifierIncremental + 1));

		this.plugin.saveConfig();

		return identifierIncremental;
	}

	public void setHorseOwner(Player player, Entity horse)
	{
		this.data.getHorsesData().set("horses."+horse.getUniqueId()+".ownerUUID", player.getUniqueId().toString());

		this.data.getHorsesData().set("horses."+horse.getUniqueId()+".identifier", getNewHorseIdentifier());

		this.data.getHorsesData().set("horses."+horse.getUniqueId()+".members", null);

		this.data.save();
	}

	public void setHorseOwner(UUID playerUUID, UUID horseUUID)
	{
		this.data.getHorsesData().set("horses."+horseUUID.toString()+".ownerUUID", playerUUID.toString());

		this.data.save();
	}

	public void setHorseOwner(String playerName, UUID horseUUID)
	{
		this.setHorseOwner(UUIDUtils.getPlayerUUID(playerName), horseUUID);
	}

	public boolean isHorseIdentifierTaken(String identifier)
	{
		ConfigurationSection horsesSection = this.data.getHorsesData().getConfigurationSection("horses");

		Boolean taken = false;

		for(String key : horsesSection.getKeys(false)){
			if (this.data.getHorsesData().getString("horses."+key+".identifier").equalsIgnoreCase(identifier))
			{
				taken = true;
			}
		}

		return taken;
	}

	public UUID getHorseUUID(String identifier)
	{
		ConfigurationSection horsesSection = this.data.getHorsesData().getConfigurationSection("horses");

		for(String key : horsesSection.getKeys(false)) {
			if (this.data.getHorsesData().isSet("horses."+key+".identifier"))
			{
				if (this.data.getHorsesData().getString("horses."+key+".identifier").equalsIgnoreCase(identifier))
				{
					return UUID.fromString(key);
				}
			}
			else
			{
				this.plugin.getLogger().warning("Horse has no identifier set (UUID "+key+") - Auto-repair");
				this.data.getHorsesData().set("horses."+key+".identifier", this.getNewHorseIdentifier());
				this.data.save();
			}
		}

		return null;
	}

    public UUID getHorseUUID(Entity horse)
    {
    	return horse.getUniqueId();
    }

	public Location getHorseLocationFromConfig(Entity horse)
	{
		return getHorseLocationFromConfig(horse.getUniqueId());
	}

	public Location getHorseLocationFromConfig(UUID horseUUID)
	{
		if (!this.data.getHorsesData().isSet("horses."+horseUUID.toString()+".lastpos")) return null;

		String locConfig = this.data.getHorsesData().getString("horses."+horseUUID.toString()+".lastpos");

		String[] locParams = locConfig.split(":");

		Location loc = new Location(Bukkit.getWorld(locParams[0]), Double.parseDouble(locParams[1]), Double.parseDouble(locParams[2]), Double.parseDouble(locParams[3]), Float.parseFloat(locParams[4]), Float.parseFloat(locParams[5]));
		return loc;
	}

    public boolean isOnHorse(Player player)
    {
    	if (player.isInsideVehicle())
    	{
    		if(isHorse(player.getVehicle().getType()))
    		{
    			return true;
    		}
    	}
    	return false;
    }

    public void ejectFromHorse(Player player)
    {
    	if (isOnHorse(player))
    	{
    		player.getVehicle().eject();
    	}
    }

	public List<String> getOwnedHorses(String playerName)
    {
    	return this.getOwnedHorses(UUIDUtils.getPlayerUUID(playerName));
    }

    public List<String> getOwnedHorses(UUID playerUUID)
    {
    	List <String> ownedHorses = new ArrayList<String>();

    	if (!this.data.getHorsesData().isConfigurationSection("horses")) return ownedHorses;

		ConfigurationSection horsesSection = this.data.getHorsesData().getConfigurationSection("horses");

		for(String key : horsesSection.getKeys(false)) {
			if (this.data.getHorsesData().isSet("horses."+key+".ownerUUID"))
			{
				UUID ownerUUID = UUID.fromString(this.data.getHorsesData().getString("horses."+key+".ownerUUID"));

				if (UUIDUtils.compareUUID(ownerUUID, playerUUID))
				{
					ownedHorses.add(key);
				}
			}
			else
			{
				this.plugin.getLogger().warning("Horse has no owner set (UUID "+key+") - Removing from config");
				this.removeHorse(UUID.fromString(key));
			}
		}

		return ownedHorses;
    }

    public boolean hasHorseIdentifier(UUID horseUUID)
    {
    	if (this.data.getHorsesData().isSet("horses."+horseUUID+".identifier")) return true;
    	return false;
    }

    public String getHorseIdentifier(UUID horseUUID)
    {
    	if (this.data.getHorsesData().isSet("horses."+horseUUID.toString()+".identifier"))
    	{
    		return this.data.getHorsesData().getString("horses."+horseUUID.toString()+".identifier");
    	}
    	return null;
    }

	// Store different horse types
	public void store(Entity horse) {
		EntityType horseType = horse.getType();

		if (horseType == EntityType.HORSE) {
			store((Horse) horse);

		} else if (horseType == EntityType.ZOMBIE_HORSE) {
			store((ZombieHorse) horse);

		} else if (horseType == EntityType.SKELETON_HORSE) {
			store((SkeletonHorse) horse);

		} else if (horseType == EntityType.DONKEY) {
			store((Donkey) horse);

		} else if (horseType == EntityType.MULE) {
			store((Mule) horse);

		} else if (horseType == EntityType.LLAMA) {
			store((Llama) horse);
		}
	}

	public void store(Horse horse) {
    	this.data.getHorsesData().set("horses."+horse.getUniqueId().toString()+".stored", true);
		saveHorseInfo(horse);
    }

    public void store(ZombieHorse horse) {
    	this.data.getHorsesData().set("horses."+horse.getUniqueId().toString()+".stored", true);
		saveHorseInfo(horse);
    }

	public void store(SkeletonHorse horse) {
    	this.data.getHorsesData().set("horses."+horse.getUniqueId().toString()+".stored", true);
		saveHorseInfo(horse);
    }

	public void store(Donkey horse) {
    	this.data.getHorsesData().set("horses."+horse.getUniqueId().toString()+".stored", true);
		saveHorseInfo(horse);
    }

	public void store(Mule horse) {
    	this.data.getHorsesData().set("horses."+horse.getUniqueId().toString()+".stored", true);
		saveHorseInfo(horse);
    }

	public void store(Llama horse) {
    	this.data.getHorsesData().set("horses."+horse.getUniqueId().toString()+".stored", true);
		saveHorseInfo(horse);
    }

	// Save details of each horse type given
	public void saveHorseInfo(ZombieHorse horse) {
		saveHorseInfo((AbstractHorse) horse);
	}

	public void saveHorseInfo(SkeletonHorse horse) {
		saveHorseInfo((AbstractHorse) horse);
	}

	public void saveHorseInfo(Donkey horse) {
		saveHorseInfo((ChestedHorse) horse);
	}

	public void saveHorseInfo(Mule horse) {
		saveHorseInfo((ChestedHorse) horse);
	}

	public void saveHorseInfo(Horse horse) {
		this.data.getHorsesData().set("horses."+horse.getUniqueId().toString()+".style", horse.getStyle().toString());
		this.data.getHorsesData().set("horses."+horse.getUniqueId().toString()+".color", horse.getColor().toString());

		if (horse.getInventory().getSaddle() != null) {
        	this.data.getHorsesData().set("horses."+horse.getUniqueId().toString()+".saddled", true);
    	} else {
			this.data.getHorsesData().set("horses."+horse.getUniqueId().toString()+".saddled", null);
		}

    	if (horse.getInventory().getArmor() != null) {
        	this.data.getHorsesData().set("horses."+horse.getUniqueId().toString()+".armor", horse.getInventory().getArmor());
    	} else {
			this.data.getHorsesData().set("horses."+horse.getUniqueId().toString()+".armor", null);
		}

		saveHorseInfo((AbstractHorse) horse);
	}

	public void saveHorseInfo(Llama horse) {
		this.data.getHorsesData().set("horses."+horse.getUniqueId().toString()+".color", horse.getColor().toString());
		this.data.getHorsesData().set("horses."+horse.getUniqueId().toString()+".strength", horse.getStrength());
		if (horse.getInventory().getDecor() != null) {
			this.data.getHorsesData().set("horses."+horse.getUniqueId().toString()+".decor", horse.getInventory().getDecor());
		}

		saveHorseInfo((ChestedHorse) horse);
	}

	public void saveHorseInfo(ChestedHorse horse) {
		if (horse.isCarryingChest()) {
			this.data.getHorsesData().set("horses."+horse.getUniqueId().toString()+".chestcontent", horse.getInventory().getContents());
		} else {
			this.data.getHorsesData().set("horses."+horse.getUniqueId().toString()+".chestcontent", null);
		}

		saveHorseInfo((AbstractHorse) horse);
	}

	public void saveHorseInfo(AbstractHorse horse) {
    	this.data.getHorsesData().set("horses."+horse.getUniqueId().toString()+".tamed", horse.isTamed());
    	this.data.getHorsesData().set("horses."+horse.getUniqueId().toString()+".jumpstrength", horse.getJumpStrength());
    	this.data.getHorsesData().set("horses."+horse.getUniqueId().toString()+".age", horse.getAge());

    	saveHorseInfo((LivingEntity) horse);
	}

	public void saveHorseInfo(LivingEntity horse) {
		Location loc = horse.getLocation();
		this.data.getHorsesData().set("horses."+horse.getUniqueId().toString()+".lastpos", loc.getWorld().getName()+":"+loc.getX()+":"+loc.getY()+":"+loc.getZ()+":"+loc.getYaw()+":"+loc.getPitch());
    	this.data.getHorsesData().set("horses."+horse.getUniqueId().toString()+".type", horse.getType().toString());
    	this.data.getHorsesData().set("horses."+horse.getUniqueId().toString()+".maxhealth", horse.getMaxHealth());

    	if (horse.getCustomName() != null) {
    		this.data.getHorsesData().set("horses."+horse.getUniqueId().toString()+".name", horse.getCustomName());
    	} else {
			this.data.getHorsesData().set("horses."+horse.getUniqueId().toString()+".name", null);
		}

    	this.data.getHorsesData().set("horses."+horse.getUniqueId().toString()+".lasthealth", horse.getHealth());

		this.shouldFlushData = true;
	}

    public boolean isStored(UUID horseUUID)
    {
    	if (this.data.getHorsesData().isSet("horses."+horseUUID+".stored"))
		{
    		return this.data.getHorsesData().getBoolean("horses."+horseUUID+".stored");
		}
    	return false;
    }

    public void summon(String horseIdentifier, Location loc)
    {
    	this.data.reload();
    	UUID horseUUID = this.getHorseUUID(horseIdentifier);
		EntityType horseType = EntityType.valueOf(this.data.getHorsesData().getString("horses."+horseUUID+".type"));

		if (horseType == EntityType.HORSE) {
			summonHorse(horseUUID, loc);

		} else if (horseType == EntityType.ZOMBIE_HORSE) {
			summonZombieHorse(horseUUID, loc);

		} else if (horseType == EntityType.SKELETON_HORSE) {
			summonSkeletonHorse(horseUUID, loc);

		} else if (horseType == EntityType.DONKEY) {
			summonDonkey(horseUUID, loc);

		} else if (horseType == EntityType.MULE) {
			summonMule(horseUUID, loc);

		} else if (horseType == EntityType.LLAMA) {
			summonLlama(horseUUID, loc);
		}

    	this.data.getHorsesData().set("horses."+horseUUID.toString(), null);
    	this.data.save();
    }

	// Summons specific types of horses and applies the relevant options to them
	public void summonHorse(UUID horseUUID, Location loc) {
		ConfigurationSection horseCfgSection = this.data.getHorsesData().getConfigurationSection("horses."+horseUUID);
    	Horse spawnedHorse = (Horse) loc.getWorld().spawnEntity(loc, EntityType.HORSE);

    	spawnedHorse.setCustomName(this.data.getHorsesData().getString("horses."+horseUUID+".name"));
    	spawnedHorse.setMaxHealth(Double.parseDouble(this.data.getHorsesData().getString("horses."+horseUUID+".maxhealth")));
    	spawnedHorse.setHealth(Double.parseDouble(this.data.getHorsesData().getString("horses."+horseUUID+".lasthealth")));
    	spawnedHorse.setJumpStrength(Double.parseDouble(this.data.getHorsesData().getString("horses."+horseUUID+".jumpstrength")));
    	spawnedHorse.setAge(Integer.parseInt(this.data.getHorsesData().getString("horses."+horseUUID+".age")));
		spawnedHorse.setColor(Horse.Color.valueOf(this.data.getHorsesData().getString("horses."+horseUUID+".color")));
		spawnedHorse.setStyle(Horse.Style.valueOf(this.data.getHorsesData().getString("horses."+horseUUID+".style")));

    	if (this.data.getHorsesData().getBoolean("horses."+horseUUID+".tamed")) {
        	spawnedHorse.setTamed(this.data.getHorsesData().getBoolean("horses."+horseUUID+".tamed"));
    	}

		if (this.data.getHorsesData().getBoolean("horses."+horseUUID+".saddled")) {
			spawnedHorse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
		}

		if (this.data.getHorsesData().getString("horses."+horseUUID+".armor") != null) {
			spawnedHorse.getInventory().setArmor((ItemStack) this.data.getHorsesData().getItemStack("horses."+horseUUID+".armor"));
		}

		this.data.getHorsesData().createSection("horses."+spawnedHorse.getUniqueId());
    	this.data.getHorsesData().set("horses."+spawnedHorse.getUniqueId(), horseCfgSection);
    	this.data.getHorsesData().set("horses."+spawnedHorse.getUniqueId()+".stored", false);
	}

	public void summonLlama(UUID horseUUID, Location loc) {
		ConfigurationSection horseCfgSection = this.data.getHorsesData().getConfigurationSection("horses."+horseUUID);
    	Llama spawnedHorse = (Llama) loc.getWorld().spawnEntity(loc, EntityType.LLAMA);

    	spawnedHorse.setCustomName(this.data.getHorsesData().getString("horses."+horseUUID+".name"));
    	spawnedHorse.setMaxHealth(Double.parseDouble(this.data.getHorsesData().getString("horses."+horseUUID+".maxhealth")));
    	spawnedHorse.setHealth(Double.parseDouble(this.data.getHorsesData().getString("horses."+horseUUID+".lasthealth")));
    	spawnedHorse.setJumpStrength(Double.parseDouble(this.data.getHorsesData().getString("horses."+horseUUID+".jumpstrength")));
    	spawnedHorse.setAge(Integer.parseInt(this.data.getHorsesData().getString("horses."+horseUUID+".age")));
		spawnedHorse.setColor(Llama.Color.valueOf(this.data.getHorsesData().getString("horses."+horseUUID+".color")));
		spawnedHorse.setStrength(this.data.getHorsesData().getInt("horses."+horseUUID+".strength"));

    	if (this.data.getHorsesData().getBoolean("horses."+horseUUID+".tamed")) {
        	spawnedHorse.setTamed(this.data.getHorsesData().getBoolean("horses."+horseUUID+".tamed"));
    	}

		if (this.data.getHorsesData().getItemStack("horses."+horseUUID+".decor") != null) {
			spawnedHorse.getInventory().setDecor(this.data.getHorsesData().getItemStack("horses."+horseUUID+".decor"));
		}

		if (this.data.getHorsesData().getList("horses."+horseUUID+".chestcontent") != null) {
			spawnedHorse.setCarryingChest(true);

			ArrayList<ItemStack> conversion = new ArrayList<ItemStack>();
			for (Object o: this.data.getHorsesData().getList("horses."+horseUUID+".chestcontent")) {
				conversion.add((ItemStack) o);
			}

			spawnedHorse.getInventory().setContents(conversion.toArray(new ItemStack[conversion.size()]));
		}

		this.data.getHorsesData().createSection("horses."+spawnedHorse.getUniqueId());
    	this.data.getHorsesData().set("horses."+spawnedHorse.getUniqueId(), horseCfgSection);
    	this.data.getHorsesData().set("horses."+spawnedHorse.getUniqueId()+".stored", false);
	}

	public void summonDonkey(UUID horseUUID, Location loc) {
		ConfigurationSection horseCfgSection = this.data.getHorsesData().getConfigurationSection("horses."+horseUUID);
    	Donkey spawnedHorse = (Donkey) loc.getWorld().spawnEntity(loc, EntityType.DONKEY);

    	spawnedHorse.setCustomName(this.data.getHorsesData().getString("horses."+horseUUID+".name"));
    	spawnedHorse.setMaxHealth(Double.parseDouble(this.data.getHorsesData().getString("horses."+horseUUID+".maxhealth")));
    	spawnedHorse.setHealth(Double.parseDouble(this.data.getHorsesData().getString("horses."+horseUUID+".lasthealth")));
    	spawnedHorse.setJumpStrength(Double.parseDouble(this.data.getHorsesData().getString("horses."+horseUUID+".jumpstrength")));
    	spawnedHorse.setAge(Integer.parseInt(this.data.getHorsesData().getString("horses."+horseUUID+".age")));

    	if (this.data.getHorsesData().getBoolean("horses."+horseUUID+".tamed")) {
        	spawnedHorse.setTamed(this.data.getHorsesData().getBoolean("horses."+horseUUID+".tamed"));
    	}

		if (this.data.getHorsesData().getString("horses."+horseUUID+".chestcontent") != null) {
			spawnedHorse.setCarryingChest(true);

			ArrayList<ItemStack> conversion = new ArrayList<ItemStack>();
			for (Object o: this.data.getHorsesData().getList("horses."+horseUUID+".chestcontent")) {
				conversion.add((ItemStack) o);
			}

			spawnedHorse.getInventory().setContents(conversion.toArray(new ItemStack[conversion.size()]));
		}

		this.data.getHorsesData().createSection("horses."+spawnedHorse.getUniqueId());
    	this.data.getHorsesData().set("horses."+spawnedHorse.getUniqueId(), horseCfgSection);
    	this.data.getHorsesData().set("horses."+spawnedHorse.getUniqueId()+".stored", false);
	}

	public void summonMule(UUID horseUUID, Location loc) {
		ConfigurationSection horseCfgSection = this.data.getHorsesData().getConfigurationSection("horses."+horseUUID);
    	Mule spawnedHorse = (Mule) loc.getWorld().spawnEntity(loc, EntityType.MULE);

    	spawnedHorse.setCustomName(this.data.getHorsesData().getString("horses."+horseUUID+".name"));
    	spawnedHorse.setMaxHealth(Double.parseDouble(this.data.getHorsesData().getString("horses."+horseUUID+".maxhealth")));
    	spawnedHorse.setHealth(Double.parseDouble(this.data.getHorsesData().getString("horses."+horseUUID+".lasthealth")));
    	spawnedHorse.setJumpStrength(Double.parseDouble(this.data.getHorsesData().getString("horses."+horseUUID+".jumpstrength")));
    	spawnedHorse.setAge(Integer.parseInt(this.data.getHorsesData().getString("horses."+horseUUID+".age")));

    	if (this.data.getHorsesData().getBoolean("horses."+horseUUID+".tamed")) {
        	spawnedHorse.setTamed(this.data.getHorsesData().getBoolean("horses."+horseUUID+".tamed"));
    	}

		if (this.data.getHorsesData().getString("horses."+horseUUID+".chestcontent") != null) {
			spawnedHorse.setCarryingChest(true);

			ArrayList<ItemStack> conversion = new ArrayList<ItemStack>();
			for (Object o: this.data.getHorsesData().getList("horses."+horseUUID+".chestcontent")) {
				conversion.add((ItemStack) o);
			}

			spawnedHorse.getInventory().setContents(conversion.toArray(new ItemStack[conversion.size()]));
		}

		this.data.getHorsesData().createSection("horses."+spawnedHorse.getUniqueId());
    	this.data.getHorsesData().set("horses."+spawnedHorse.getUniqueId(), horseCfgSection);
    	this.data.getHorsesData().set("horses."+spawnedHorse.getUniqueId()+".stored", false);
	}

	public void summonZombieHorse(UUID horseUUID, Location loc) {
		ConfigurationSection horseCfgSection = this.data.getHorsesData().getConfigurationSection("horses."+horseUUID);
    	ZombieHorse spawnedHorse = (ZombieHorse) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE_HORSE);

    	spawnedHorse.setCustomName(this.data.getHorsesData().getString("horses."+horseUUID+".name"));
    	spawnedHorse.setMaxHealth(Double.parseDouble(this.data.getHorsesData().getString("horses."+horseUUID+".maxhealth")));
    	spawnedHorse.setHealth(Double.parseDouble(this.data.getHorsesData().getString("horses."+horseUUID+".lasthealth")));
    	spawnedHorse.setJumpStrength(Double.parseDouble(this.data.getHorsesData().getString("horses."+horseUUID+".jumpstrength")));
    	spawnedHorse.setAge(Integer.parseInt(this.data.getHorsesData().getString("horses."+horseUUID+".age")));

    	if (this.data.getHorsesData().getBoolean("horses."+horseUUID+".tamed")) {
        	spawnedHorse.setTamed(this.data.getHorsesData().getBoolean("horses."+horseUUID+".tamed"));
    	}

		this.data.getHorsesData().createSection("horses."+spawnedHorse.getUniqueId());
    	this.data.getHorsesData().set("horses."+spawnedHorse.getUniqueId(), horseCfgSection);
    	this.data.getHorsesData().set("horses."+spawnedHorse.getUniqueId()+".stored", false);
	}

	public void summonSkeletonHorse(UUID horseUUID, Location loc) {
		ConfigurationSection horseCfgSection = this.data.getHorsesData().getConfigurationSection("horses."+horseUUID);
    	SkeletonHorse spawnedHorse = (SkeletonHorse) loc.getWorld().spawnEntity(loc, EntityType.SKELETON_HORSE);

    	spawnedHorse.setCustomName(this.data.getHorsesData().getString("horses."+horseUUID+".name"));
    	spawnedHorse.setMaxHealth(Double.parseDouble(this.data.getHorsesData().getString("horses."+horseUUID+".maxhealth")));
    	spawnedHorse.setHealth(Double.parseDouble(this.data.getHorsesData().getString("horses."+horseUUID+".lasthealth")));
    	spawnedHorse.setJumpStrength(Double.parseDouble(this.data.getHorsesData().getString("horses."+horseUUID+".jumpstrength")));
    	spawnedHorse.setAge(Integer.parseInt(this.data.getHorsesData().getString("horses."+horseUUID+".age")));

    	if (this.data.getHorsesData().getBoolean("horses."+horseUUID+".tamed")) {
        	spawnedHorse.setTamed(this.data.getHorsesData().getBoolean("horses."+horseUUID+".tamed"));
    	}

		this.data.getHorsesData().createSection("horses."+spawnedHorse.getUniqueId());
    	this.data.getHorsesData().set("horses."+spawnedHorse.getUniqueId(), horseCfgSection);
    	this.data.getHorsesData().set("horses."+spawnedHorse.getUniqueId()+".stored", false);
	}

    public HorseTeleportResponse teleportHorse(UUID horseUUID, Location loc)
    {
    	if (this.data.getHorsesData().getBoolean("horses."+horseUUID.toString()+".stored"))
    	{
    		return HorseTeleportResponse.NOT_TELEPORTED_STORED;
    	}

    	for(World w: this.plugin.getServer().getWorlds()){
            for(LivingEntity e: w.getLivingEntities()){

                if(horseUUID.toString().equalsIgnoreCase(e.getUniqueId().toString())){

                	if (!e.getLocation().getChunk().isLoaded()) {
                		e.getLocation().getChunk().load();
                	}

                	e.teleport(loc);
                	return HorseTeleportResponse.TELEPORTED;
                }

            }
        }

		if (this.getHorseLocationFromConfig(horseUUID) != null)
		{
			Location horseLastLocation = this.getHorseLocationFromConfig(horseUUID);
			Chunk c = horseLastLocation.getChunk();

        	if (!horseLastLocation.getWorld().getName().equalsIgnoreCase(loc.getWorld().getName()))
        	{
        		this.plugin.getLogger().warning("Tried to teleport horse in another world ("+horseLastLocation.getWorld().getName()+" to "+loc.getWorld().getName());
        		return HorseTeleportResponse.NOT_TELEPORTED_WRONG_WORLD;
        	}

			if (!c.isLoaded()) {
				c.load();
			}

			Entity[] entitiesChunkList = c.getEntities();
			for(Entity e: entitiesChunkList){
				if (e.getUniqueId().toString().equalsIgnoreCase(horseUUID.toString()))
				{
					e.teleport(loc);
					return HorseTeleportResponse.TELEPORTED;
				}
			}

			// If this gets hit something probably went wrong, so force the horse to stored
			// and attempt to transparently create a new one from the info that's available
			this.data.getHorsesData().set("horses."+horseUUID.toString()+".stored", true);
			this.data.save();
			summon(getHorseIdentifier(horseUUID), loc);
			return HorseTeleportResponse.TELEPORTED;
		}

		return HorseTeleportResponse.NOT_TELEPORTED;
    }

}
