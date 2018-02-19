package com.rubdub;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

class CommandHomeToggle implements CommandExecutor {
	private Main main;

	public CommandHomeToggle(Main plugin) {
		this.main = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (player.getUniqueId().toString().equals("7dfce941-5b9b-4f4e-9742-4fa9fe384d0b")
					|| Main.plist.contains(sender.getName().toLowerCase())) {
				if (cmd.getName().equalsIgnoreCase("home")) {

					if (args.length >= 1) {

						if (args.length == 1) {
							if (args[0].equalsIgnoreCase("home")) {
								// Bukkit.getServer().broadcastMessage("asda");

								//Main.homeBool = true ^ Main.homeBool;
								Main.homingmap.put(player, (true ^ Main.homingmap.get(player)));
								player.sendMessage("Homing: " + Main.homeBool);
								return true;

							} else if (args[0].equalsIgnoreCase("explode")) {
								//Main.explode = true ^ Main.explode;
								Main.explodemap.put(player, (true ^ Main.explodemap.get(player)));
								player.sendMessage("Exploding arrows: " + Main.explode);
								return true;
							}

							else if (args[0].equalsIgnoreCase("fire")) {
								//Main.fireArrows = true ^ Main.fireArrows;
								Main.firemap.put(player, (true^Main.firemap.get(player)));
								player.sendMessage("Fire arrows: " + Main.fireArrows);
								return true;
							}
							else if(args[0].equalsIgnoreCase("gravity")){
								Main.nograv = true ^ Main.nograv;
								player.sendMessage("Gravity: " + (true^Main.nograv));
								return true;
							}
						} else if (args[0].equalsIgnoreCase("allow")) {

							if (args.length >= 2) {
								String name = args[1].toLowerCase();

								if (!Main.plist.contains(name)) {
									Main.plist.add(name);
									sender.sendMessage("Allowed " + name);
									sender.sendMessage(Main.plist.toString());
									// String[] a = new String[1];
									// a[0]=name;
									main.getConfig().set("players.yee", Main.plist.toArray());
									main.saveConfig();
									// sender.sendMessage(main.getConfig().getStringList("players.yee").toArray().toString());
									return true;
								} else if (args.length == 1) {
									sender.sendMessage(name + " is already allowed");
									return true;
								}

							} else {
								sender.sendMessage(Main.plist.toArray().toString());
								return true;
							}

						} else if (args[0].equalsIgnoreCase("disallow")) {
							String name = args[1].toLowerCase();
							if (Main.plist.contains(name)) {
								Main.plist.remove(name);
								sender.sendMessage("Disallowed " + name);
								sender.sendMessage(Main.plist.toString());
								main.getConfig().set("players.yee", Main.plist.toArray());
								main.saveConfig();
								return true;
							} else {
								sender.sendMessage(name + " is already not allowed");
								return true;
							}

						} else
							return false;

					} else if (args.length == 0) {
						player.sendMessage("Homing (/home home): " + Main.homeBool);
						player.sendMessage("Exploding arrows (/home explode): " + Main.explode);
						player.sendMessage("Fire arrows (/home fire): " + Main.fireArrows);
						player.sendMessage("Gravity (/home gravity): " + (true^Main.nograv));
						return true;
					} else
						return false;

				}

			}

			return false;
		}
		return false;
	}

}

class clearArrow extends BukkitRunnable {
	private Set<Arrow> al;

	public clearArrow(Set<Arrow> stuckSet) {
		this.al = stuckSet;

	}

	@Override
	public void run() {
		// ArrayList<Entity> al = new ArrayList();
		// al=(ArrayList<Entity>) world.getEntities();
		// Bukkit.getServer().broadcastMessage("baka");
		for (Arrow arrow : al) {

			arrow.remove();

		}
	}
}

public class Main extends JavaPlugin implements Listener {
	static Set<UUID> projectileSet = new HashSet();
	static Set<Arrow> stuckSet = new HashSet();

	
	
	static HashMap<Player,Boolean> homingmap = new HashMap();
	static HashMap<Player,Boolean> firemap = new HashMap();
	static HashMap<Player,Boolean> gravmap = new HashMap();
	static HashMap<Player,Boolean> explodemap = new HashMap();
	// static ArrayList<String> allowedPlayers = new ArrayList();

	static boolean nograv = false;
	static boolean fireArrows = false;
	static boolean homeBool = true;
	static boolean explode = false;

	static List<String> plist = new LinkedList();

	BukkitScheduler scheduler = getServer().getScheduler();

	FileConfiguration config = getConfig();

	private void createConfig() {
		try {
			if (!getDataFolder().exists()) {
				getDataFolder().mkdirs();
			}
			File file = new File(getDataFolder(), "config.yml");
			if (!file.exists()) {
				getLogger().info("config.yml not found, creating!");

				config.addDefault("players.yee", plist.toArray());
				config.options().copyDefaults(true);
				saveConfig();
			} else {
				getLogger().info("config.yml found, loading!");

			}
		} catch (Exception e) {
			e.printStackTrace();

		}

	}

	@Override
	public void onEnable() {
		createConfig();
		plist = config.getStringList("players.yee");
		getServer().getPluginManager().registerEvents(this, this);
		getServer().broadcastMessage("lmao mah plugins up");
		new clearArrow(stuckSet).runTaskTimer(this, 0L, 10L);
		this.getCommand("home").setExecutor(new CommandHomeToggle(this));

	}

	@Override
	public void onDisable() {
		this.scheduler.cancelAllTasks();
		try {
			config.save("config.yml");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@EventHandler
	public static void onProjectileHit(ProjectileHitEvent event) {
		Entity entity = event.getEntity();
		if (event.getHitEntity() == null) {
			if (entity instanceof Arrow) {
				stuckSet.add((Arrow) entity);
				if (((Arrow) entity).getShooter() instanceof Player) {
					Player player = (Player) ((Projectile) entity).getShooter();
					if (player.getUniqueId().toString().equals("7dfce941-5b9b-4f4e-9742-4fa9fe384d0b")) {
						Arrow arrow = (Arrow) entity;

					}
				}
			}

		}
	}

	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		homingmap.put(player, false);
		firemap.put(player, false);
		gravmap.put(player, false);
		explodemap.put(player, false);
	}
	
	
	/*
	 * @EventHandler public void onInteract(PlayerInteractEvent event) { Player
	 * player = event.getPlayer(); ItemStack item =
	 * player.getInventory().getItemInMainHand(); if (item.getType() ==
	 * Material.FERMENTED_SPIDER_EYE && event.getAction() ==
	 * Action.RIGHT_CLICK_AIR && player.getUniqueId().toString().equals(
	 * "7dfce941-5b9b-4f4e-9742-4fa9fe384d0b")) { crazy ^= true;
	 * player.sendMessage("crazy: " + crazy); } if (item.getType() ==
	 * Material.RABBIT_FOOT && event.getAction() == Action.RIGHT_CLICK_AIR &&
	 * player.getUniqueId().toString().equals(
	 * "7dfce941-5b9b-4f4e-9742-4fa9fe384d0b")) { homeBool ^= true;
	 * player.sendMessage("homing: " + homeBool); }
	 * 
	 * }
	 */
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity damager = event.getDamager();
		if (damager instanceof Arrow) {
			projectileSet.remove(damager.getUniqueId());
			if (((Projectile) damager).getShooter() instanceof Player) {
				Player player = (Player) ((Projectile) damager).getShooter();
				if (player.getUniqueId().toString().equals("7dfce941-5b9b-4f4e-9742-4fa9fe384d0b")) {
					// ((LivingEntity)
					// event.getEntity()).setMaximumNoDamageTicks(0);
					// ((LivingEntity) event.getEntity()).damage(1);
				}
			}
		}

	}

	/*
	 * @EventHandler public void onEntityDead(EntityDeathEvent event){ Entity
	 * entity = event.getEntity(); getServer().broadcastMessage(((LivingEntity)
	 * entity).getKiller().getType().toString()); if(entity instanceof
	 * LivingEntity){ LivingEntity killer = ((LivingEntity)entity).getKiller();
	 * if(killer instanceof Arrow){ LivingEntity killersource = (LivingEntity)
	 * ((Arrow) killer).getShooter(); if(killersource instanceof Player &&
	 * crazy){ if(killersource.getUniqueId().toString().equals(
	 * "7dfce941-5b9b-4f4e-9742-4fa9fe384d0b")){ TNTPrimed tnt =
	 * (TNTPrimed)entity.getWorld().spawnEntity(entity.getLocation(),
	 * EntityType.PRIMED_TNT); tnt.setFuseTicks(0); tnt.setYield(2); } } } } }
	 * 
	 */
	@EventHandler
	public void onFireArrow(EntityShootBowEvent event) {

		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			Arrow arrow = (Arrow) event.getProjectile();
			projectileSet.add(arrow.getUniqueId());
			// getServer().broadcastMessage("hubdub");
			// getServer().broadcastMessage(player.getUniqueId().toString());
			if (player.getUniqueId().toString().equals("7dfce941-5b9b-4f4e-9742-4fa9fe384d0b")
					|| player.getUniqueId().toString().equals("944d71ac-bcbd-491e-b5be-b0f36ecc3c31")
					|| player.getUniqueId().toString().equals("c79c738c-2ef5-4311-86a0-4062084dcc16")
					|| plist.contains(player.getName().toLowerCase())) {
				Location ploc = player.getLocation();
				// getServer().broadcastMessage("hubb");
				if (homeBool || nograv) {
					arrow.setGravity(false);
				}
				if (homeBool || fireArrows || explode||nograv) {
					arrow.setCritical(true);
					arrow.setSilent(true);
					arrow.setGlowing(true);
				}
				// getServer().broadcastMessage(closeEnt.getType().toString());
				// player.sendMessage(closeEnt.getType().toString());

				new HomingArrowTask(player, arrow, this).runTaskTimer(this, 10L, 1L);

			}
		}
	}

}

class HomingArrowTask extends BukkitRunnable {

	private ProjectileSource player;
	private Arrow arrow;
	// private LivingEntity entity;
	private Plugin plugin;
	private final Vector aVel;

	public HomingArrowTask(ProjectileSource player, Arrow arrow, Plugin plugin) {
		this.player = player;
		this.arrow = arrow;
		// this.entity = entity;
		this.plugin = plugin;
		this.aVel = arrow.getVelocity();
	}

	@Override
	public void run() {
		LivingEntity closeEnt = null;
		double distance = 0;
		double mindis = 140;
		Location aloc = arrow.getLocation();
		for (Entity entity : arrow.getNearbyEntities(140, 140, 140)) {

			if (entity instanceof LivingEntity && entity != player
					&& !entity.getUniqueId().toString().equals("7dfce941-5b9b-4f4e-9742-4fa9fe384d0b")) {
				boolean player = false;
				if (entity instanceof Player)
					player = true;
				boolean yesno = false;
				if (player && ((Player) entity).getGameMode() != GameMode.CREATIVE
						&& ((Player) entity).getGameMode() != GameMode.SPECTATOR)
					yesno = true;
				if ((player && yesno) || !player) {
					Location loc = entity.getLocation();

					double x = (aloc.getX() - loc.getX());
					double y = (aloc.getY() - loc.getY());
					double z = (aloc.getZ() - loc.getZ());

					distance = Math.sqrt((Math.pow(x, 2) + Math.pow(y, 2)) + Math.pow(z, 2));
					// getServer().broadcastMessage("hub");
					if (mindis > distance) {
						mindis = distance;
						closeEnt = (LivingEntity) entity;
					}
				}

			}
		}
		// player.sendMessage(closeEnt.getType().toString());

		// player.teleport(entity);
		if (arrow.isDead() || aVel.length() == 0 || !Main.projectileSet.contains(arrow.getUniqueId())
				|| Main.stuckSet.contains(arrow)) {
			if (Main.explode
					&& ((((Player) player).getUniqueId().toString().equals("7dfce941-5b9b-4f4e-9742-4fa9fe384d0b"))
					|| ((Player) player).getUniqueId().toString().equals("944d71ac-bcbd-491e-b5be-b0f36ecc3c31")
					|| Main.plist.contains(((Player) player).getName().toLowerCase()))) {
				TNTPrimed fr = (TNTPrimed) arrow.getWorld().spawnEntity(arrow.getLocation(), EntityType.PRIMED_TNT);
				fr.setFuseTicks(0);
				fr.setYield((int) Math.floor(aVel.length()) * 5);
				// Bukkit.getServer().broadcastMessage(Double.toString(aVel.length()));
			}
			arrow.remove();

			if (Main.stuckSet.contains(arrow))
				Main.stuckSet.remove(arrow);
			this.cancel();
		}
		if (closeEnt.isDead())
			arrow.teleport(arrow.getLocation().add(0, 1, 0));
		Location loc = arrow.getLocation();
		Location eloc = closeEnt.getEyeLocation();
		// Main.onProjectileHit(ProjectileHitEvent, hitent, arrow);
		double mag = aVel.length();
		double x = eloc.getX() - loc.getX();
		double y = eloc.getY() - loc.getY();
		double z = eloc.getZ() - loc.getZ();
		Vector v = new Vector(x, y, z);
		v.multiply(1 / v.length());
		v.multiply((mag + 0.3 * (mag)));
		Vector va = new Vector(x, y, z);
		va.multiply(1 / va.length());
		// va = va.multiply(2);
		if ((((Entity) player).getUniqueId().toString().equals("944d71ac-bcbd-491e-b5be-b0f36ecc3c31")
				|| ((Entity) player).getUniqueId().toString().equals("7dfce941-5b9b-4f4e-9742-4fa9fe384d0b")
				|| Main.plist.contains(((Player) player).getName().toLowerCase())) && Main.fireArrows) {
			// Arrow a = (Arrow) arrow.getWorld().spawnEntity(loc.add(0,1,0),
			// EntityType.ARROW);
			Arrow b = (Arrow) arrow.getWorld().spawnEntity(loc, EntityType.ARROW);
			// Arrow aa = (Arrow) arrow.getWorld().spawnEntity(loc.add(0,1,0),
			// EntityType.ARROW);
			// TNTPrimed fr = (TNTPrimed)
			// arrow.getWorld().spawnEntity(loc.subtract(0,3,0),
			// EntityType.PRIMED_TNT);
			// fr.setFuseTicks(20);
			Vector vt = aVel.clone();
			if (Main.homeBool)
				b.setVelocity((va.subtract((b.getLocation().subtract(loc)).toVector())).multiply(10));
			else
				b.setVelocity(vt.normalize().multiply(4));

			b.setBounce(false);
			// fr.setVelocity(v);
			// a.setCritical(true);
			// aa.setCritical(true);
			b.setCritical(true);
			b.setKnockbackStrength(5);
			// b.setGravity(false);
			b.setSilent(true);
			b.setFireTicks(20);
			b.setShooter(player);
			// a.setGravity(false);

			// a.setVelocity(((((Entity)player).getLocation().getDirection().normalize().multiply(2)).subtract(v)));
			// aa.setVelocity(((((Entity)player).getLocation().getDirection().normalize().multiply(2)).add(v)));
			// aa.setBounce(false);
			// a.setBounce(false);
		}
		// Arrow a = (Arrow)
		// ((ProjectileSource)arrow).launchProjectile(Arrow.class);

		/*
		 * player.sendMessage("hoyter"); Arrow a = (Arrow) ((ProjectileSource)
		 * arrow).launchProjectile(Arrow.class); a.setVelocity(v.multiply(2));
		 * a.setShooter(player);
		 * 
		 */
		if(Main.nograv&&!Main.homeBool){
			Vector vv = aVel.clone();
			double m = vv.length();
			vv.multiply(1/m);
			vv.normalize();
			vv.multiply(m+(m)*3);
			
			arrow.setVelocity(vv);
		}
		if (Main.homeBool)
			arrow.setVelocity(v);

	}

}
