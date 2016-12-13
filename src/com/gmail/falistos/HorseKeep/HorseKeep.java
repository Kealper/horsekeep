package com.gmail.falistos.HorseKeep;

import com.gmail.falistos.HorseKeep.commands.*;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class HorseKeep extends JavaPlugin implements Listener
{
	public String version = "0.3.6";

	public Permission perm = null;
	public HorseManager manager = null;
	public Locale lang = null;
	public HorseData data = null;

	public String prefix = "HorseKeep";

	public void onEnable()
	{
		// Configuration
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();

		// Chat prefix
		if (getConfig().get("chatPrefix") != null)
		{
			this.prefix = (String) getConfig().get("chatPrefix");
		}

		// Horses Data
		this.data = new HorseData(this, "horses.yml");
		this.data.migrate();
		this.data.migrateUUID();

		// Locale
		this.lang = new Locale(this, "language_"+getConfig().getString("language"),"language_en.yml");
		lang.saveDefault();

		// Metrics
		if (getConfig().getBoolean("enableMetrics"))
		{
			try {
			    MetricsLite metrics = new MetricsLite(this);
			    metrics.start();
			} catch (IOException e) {
			    getLogger().info("Can't submit plugin usage data to Metrics");
			}
		}

		// Vault
		if(Bukkit.getPluginManager().getPlugin("Vault") == null) {
			getLogger().severe("Vault is not installed, this plugin require Vault");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		setupPermissions();

		// Horses Manager
		this.manager = new HorseManager(this);

		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info("Enabled");
	}

	public HorseData getHorseData() { return this.data; }

	public String getChatPrefix()
	{
		return ChatColor.RED + "[" + ChatColor.GOLD + this.prefix + ChatColor.RED + "] " + ChatColor.GREEN;
	}

	private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            perm = permissionProvider.getProvider();
        }
        return (perm != null);
    }

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("horse")) {
			if (args.length == 0)
			{
				new CommandHelp(this, sender, args);
			}
			else if (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("l"))
			{
				new CommandList(this, sender, args);
			}
			else if (args[0].equalsIgnoreCase("setid") || args[0].equalsIgnoreCase("id"))
			{
				new CommandSetIdentifier(this, sender, args);
			}
			else if (args[0].equalsIgnoreCase("tp"))
			{
				new CommandTeleport(this, sender, args);
			}
			else if (args[0].equalsIgnoreCase("tpall"))
			{
				new CommandTeleportAll(this, sender, args);
			}
			else if (args[0].equalsIgnoreCase("members") || args[0].equalsIgnoreCase("m"))
			{
				new CommandMembers(this, sender, args);
			}
			else if (args[0].equalsIgnoreCase("addmember") || args[0].equalsIgnoreCase("addm"))
			{
				new CommandAddMember(this, sender, args);
			}
			else if (args[0].equalsIgnoreCase("delmember") || args[0].equalsIgnoreCase("delm"))
			{
				new CommandDeleteMember(this, sender, args);
			}
			else if (args[0].equalsIgnoreCase("unprotect") || args[0].equalsIgnoreCase("up"))
			{
				new CommandUnprotect(this, sender, args);
			}
			else if (args[0].equalsIgnoreCase("store"))
			{
				new CommandStore(this, sender, args);
			}
			else if (args[0].equalsIgnoreCase("summon"))
			{
				new CommandSummon(this, sender, args);
			}
			else if (args[0].equalsIgnoreCase("getid"))
			{
				new CommandGetIdentifier(this, sender, args);
			}
			else if (args[0].equalsIgnoreCase("reload"))
			{
				new CommandReload(this, sender, args);
			}
			else if (args[0].equalsIgnoreCase("info"))
			{
				sender.sendMessage(this.getChatPrefix() + "HorseKeeper version "+this.version+" - Created by Falistos/BritaniaCraft (falistos@gmail.com)");
			}
			else if (args[0].equalsIgnoreCase("admin"))
			{

				if (!perm.has(sender, "horsekeep.admin"))
				{
					sender.sendMessage(this.getChatPrefix() + ChatColor.RED + this.lang.get("noPermission"));
					return true;
				}

				if (args.length < 2) return true;

				if (args[1].equalsIgnoreCase("list"))
				{
					new CommandAdminList(this, sender, args);
				}
				else if (args[1].equalsIgnoreCase("transfer"))
				{
					new CommandAdminTransfer(this, sender, args);
				}

			}
			return true;
		}
		return false;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityDamage(EntityDamageEvent e) {
        if (e.isCancelled()) {
            return;
        }

		if (this.manager.isHorse(e.getEntity()))
		{
			LivingEntity horse = (LivingEntity) e.getEntity();

			if (this.manager.isOwned(horse.getUniqueId()))
			{
				if(e instanceof EntityDamageByEntityEvent)
				{
					EntityDamageByEntityEvent e1 = (EntityDamageByEntityEvent) e;

			        if (e1.getDamager() instanceof Player)
			        {
				        Player damager = (Player) e1.getDamager();

				        if(!perm.has(damager, "horsekeep.bypass.protection"))
				        {
					        if(getConfig().getBoolean("disableHorseDamage"))
					        {
					        	damager.sendMessage(this.getChatPrefix() + ChatColor.GOLD + this.lang.get("cantAttackOwnedHorse"));
					            e1.setCancelled(true);
					        }
					        else if(this.manager.canMountHorse(damager, horse) && getConfig().getBoolean("disableHorseDamageFromMembers")) {
					        	damager.sendMessage(this.getChatPrefix() + ChatColor.GOLD + this.lang.get("cantAttackOwnedHorseIfMember"));
					            e1.setCancelled(true);
					        }
				        }
			        }
			        else if (e1.getDamager() instanceof Projectile)
			        {
			        	Projectile projectile = (Projectile) e1.getDamager();

				  	    if (projectile.getShooter() instanceof Player)
					    {
					        Player shooter = (Player) projectile.getShooter();

					        	if(!perm.has(shooter, "horsekeep.bypass.protection"))
				  				{
					  	        	if(getConfig().getBoolean("disableHorseDamage"))
					  	        	{
					  	        		shooter.sendMessage(this.getChatPrefix() + ChatColor.GOLD + this.lang.get("cantAttackOwnedHorse"));
					  	        		e.setCancelled(true);
					  	        	}
					  	        	else if(this.manager.canMountHorse(shooter, horse) && getConfig().getBoolean("disableHorseDamageFromMembers")) {
					  	        		shooter.sendMessage(this.getChatPrefix()  + ChatColor.GOLD + this.lang.get("cantAttackOwnedHorseIfMember"));
					  	        		e.setCancelled(true);
					  	        	}
				  				}
					     }
				  	     else if (getConfig().getBoolean("disableHorseEnvironmentalDamage"))
				  	     {
				  	    	 e.setCancelled(true);
				  	     }
			        }
			        else if (getConfig().getBoolean("disableHorseEnvironmentalDamage"))
			        {
			        	e.setCancelled(true);
			        }
				}
		        else if (getConfig().getBoolean("disableHorseEnvironmentalDamage"))
		        {
		        	e.setCancelled(true);
		        }
			}
		}

	}

	@EventHandler(priority = EventPriority.HIGH)
    public void PotionsSplash(PotionSplashEvent e){
        if (e.isCancelled()) {
            return;
        }

        if(e.getEntity().getShooter() instanceof Player){
                Player thrower = (Player) e.getEntity().getShooter();
                Collection<LivingEntity> AffectedEntities = e.getAffectedEntities();
                Iterator<LivingEntity> it = AffectedEntities.iterator();

                boolean cancelEvent = false;

                String message = null;

                while (it.hasNext())
                {
                	LivingEntity entity = it.next();
                	if (this.manager.isHorse(entity))
                	{
                		if(this.manager.isOwned(entity.getUniqueId()) && !perm.has(thrower, "horsekeep.bypass.protection"))
        				{
                    		if (getConfig().getBoolean("disableHorseDamage"))
        	  	        	{
        	  	        		message = this.getChatPrefix() + this.lang.get("cantAttackOwnedHorse");
        	  	        		cancelEvent = true;
        	  	        	}
        	  	        	else if(this.manager.canMountHorse(thrower, entity) && getConfig().getBoolean("disableHorseDamageFromMembers")) {
        	  	        		message = this.getChatPrefix() + this.lang.get("cantAttackOwnedHorseIfMember");
        	  	        		cancelEvent = true;
        	  	        	}
        				}
                	}
                }

                if (cancelEvent)
                {
                	thrower.sendMessage(message);
                	e.setCancelled(true);
                }
        }
    }

	@EventHandler(priority = EventPriority.HIGH)
    public void onDeath(EntityDeathEvent e) {
        Entity entity = e.getEntity();

        if(this.manager.isHorse(entity)) {
            if (this.manager.isOwned(entity.getUniqueId()))
            {
            	UUID ownerUUID = this.manager.getOwner(entity.getUniqueId());
            	Player owner = Bukkit.getPlayer(ownerUUID);

            	if (owner.isOnline()) owner.sendMessage(this.getChatPrefix() + ChatColor.RED + this.lang.get("horseDead").replace("%id", this.manager.getHorseIdentifier(entity.getUniqueId())));

            	this.manager.removeHorse(this.manager.getHorseIdentifier(entity.getUniqueId()));
            }
        }
    }


	@EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnloaded(ChunkUnloadEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Chunk c = event.getChunk();

        Entity[] entities = c.getEntities();

        for(Entity e: entities){
        	if (this.manager.isHorse(e))
        	{
        		if (this.manager.isOwned(e.getUniqueId()))
        		{
					EntityType horseType = e.getType();

        			// We save horse location
					if (horseType == EntityType.HORSE) {
						this.manager.saveHorseInfo((Horse) e);

					} else if (horseType == EntityType.ZOMBIE_HORSE) {
						this.manager.saveHorseInfo((ZombieHorse) e);

					} else if (horseType == EntityType.SKELETON_HORSE) {
						this.manager.saveHorseInfo((SkeletonHorse) e);

					} else if (horseType == EntityType.DONKEY) {
						this.manager.saveHorseInfo((Donkey) e);

					} else if (horseType == EntityType.MULE) {
						this.manager.saveHorseInfo((Mule) e);

					} else if (horseType == EntityType.LLAMA) {
						this.manager.saveHorseInfo((Llama) e);
					}
        		}
        	}
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityInteract(PlayerInteractEntityEvent event)
    {
      if (event.isCancelled()) {
          return;
      }

      if (this.manager.isHorse(event.getRightClicked()))
      {

        LivingEntity horse = (LivingEntity) event.getRightClicked();

        if (!this.manager.isOwned(horse.getUniqueId()))
        {
        	if (event.getPlayer().getItemInHand().getType() == Material.SADDLE)
        	{
        		if (!perm.has(event.getPlayer(), "horsekeep.protect") && !perm.has(event.getPlayer(), "horsekeep.admin"))
				{
        			return;
				}

        		List<Map<?, ?>> groups;

        		groups = getConfig().getMapList("groups");

        		int limit = getConfig().getInt("horsesDefaultLimit");

        		if (!perm.has(event.getPlayer(), "horsekeep.bypass.limit"))
				{
            		for(Map<?, ?> item : groups){

            			for(Entry<?, ?> group : item.entrySet())
            			{
                			if (perm.has(event.getPlayer(), "horsekeep.groups."+group.getKey()))
                			{
                				limit = (Integer) group.getValue();
                			}
            			}
            		}

            		if (this.manager.getOwnedHorses(event.getPlayer().getUniqueId()).size() >= limit)
            		{
            			event.getPlayer().sendMessage(this.getChatPrefix() + ChatColor.GOLD + this.lang.get("horsesLimitReached"));
            			return;
            		}
				}

        		if (event.getPlayer().getName().isEmpty()) { event.getPlayer().sendMessage(this.getChatPrefix() + ChatColor.RED + "Error while setting up protection"); return; }

        		this.manager.setHorseOwner(event.getPlayer(), horse);

        		horse.getWorld().playSound(
        		          horse.getLocation(),
        		          Sound.ENTITY_PLAYER_LEVELUP, 10.0F, 1.0F);

        		event.getPlayer().sendMessage(this.getChatPrefix() + this.lang.get("horseProtected").replace("%id", this.manager.getHorseIdentifier(this.manager.getHorseUUID(horse))));

        		if (getConfig().getBoolean("autoTameHorseOnProtect")) {
					Tameable tameableEntity = (Tameable) event.getRightClicked();
					tameableEntity.setTamed(true);
				}

				this.manager.saveHorseInfo(horse);
        	}
        }
        else
        {
        	if (!this.manager.canMountHorse(event.getPlayer(), horse) && !perm.has(event.getPlayer(), "horsekeep.bypass.mount"))
        	{
    			String ownerName = this.manager.getOwnerName(horse.getUniqueId());

		    	if (ownerName == null) ownerName = "Unknown";

            	event.getPlayer().sendMessage(this.getChatPrefix() + ChatColor.RED + this.lang.get("horseBelongsTo").replace("%player", ownerName));

            	if (perm.has(event.getPlayer(), "horsekeep.admin")) { event.getPlayer().sendMessage(this.getChatPrefix() + this.lang.get("identifier") + " " + this.manager.getHorseIdentifier(this.manager.getHorseUUID(horse))); }

            	event.setCancelled(true);
        	}
        }
      }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onAnimalTame(EntityTameEvent e)
    {
        if (e.isCancelled()) {
            return;
        }

    	LivingEntity tamedAnimal = e.getEntity();
    	Player player = (Player) e.getOwner();

    	if (this.manager.isHorse(tamedAnimal) && !this.manager.isOwned(tamedAnimal.getUniqueId()))
    	{
    		player.sendMessage(this.getChatPrefix() + this.lang.get("tamedHorse"));
    	}
    }

	@EventHandler(priority = EventPriority.HIGH)
    public void onVehicleEnter(VehicleEnterEvent event)
    {
        if (event.isCancelled()) {
            return;
        }

		if (!(event.getEntered() instanceof Player)) { return; }

		Player player = (Player) event.getEntered();

	    if (this.manager.isHorse(event.getVehicle()))
	    {
	    	LivingEntity horse = (LivingEntity) event.getVehicle();

	    	if (this.manager.isOwned(horse.getUniqueId()))
	    	{
	    		if (!this.manager.canMountHorse(player, horse) && !perm.has(player, "horsekeep.bypass.mount"))
	    		{
	    			String ownerName = this.manager.getOwnerName(horse.getUniqueId());

			    	if (ownerName == null) ownerName = "Unknown";

	    			player.sendMessage(this.getChatPrefix() + this.lang.get("horseBelongsTo").replace("%player", ownerName));

	    			event.setCancelled(true);
	    		}
	    	}
	    }
    }

	public void onDisable() { }
}