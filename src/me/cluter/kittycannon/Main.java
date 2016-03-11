package me.cluter.kittycannon;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;

public class Main extends JavaPlugin implements Listener {

	public void onEnable() {
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
	}

	@EventHandler
	public void catCannon(PlayerInteractEvent ev) {
		Random random = new Random();
         final Player p = ev.getPlayer();
		if ((p.getItemInHand().getType() != Material.STICK))
			return;
		if ((ev.getAction() == Action.RIGHT_CLICK_BLOCK || ev.getAction() == Action.RIGHT_CLICK_AIR)) {
			String display = null;
			try {
				display = p.getItemInHand().getItemMeta().getDisplayName();
			} catch (NullPointerException e1) {
			}
			if (display == null) {
				return;
			}
			if (display.contains("Kitty Cannon")) {
				Location l = p.getEyeLocation();
				Entity tar = null;
				double actdis = 5000000;
				for (Entity e : p.getNearbyEntities(50, 50, 50)) {
					if (e instanceof LivingEntity) {
						if ((e.equals(p)))
							continue;
						if (e instanceof Ocelot)
							continue;
						double distance = p.getLocation().distance(e.getLocation());
						if (distance < actdis) {
							actdis = distance;
							tar = e;
						}
					}
				}
				final Entity tar2 = tar;

				if (tar != null) {
					final Ocelot cat = (Ocelot) p.getWorld().spawnEntity(l, EntityType.OCELOT);
					int i = random.nextInt(Ocelot.Type.values().length);
					cat.setCatType(Ocelot.Type.values()[i]);
					cat.setVelocity(p.getEyeLocation().getDirection().multiply(2));
					cat.setCustomName("Meow");
					final Location loc = cat.getLocation();
					final Location vicLoc = tar.getLocation();
					vicLoc.add(0, 0.5, 0);
					new BukkitRunnable() {
						public void run() {
							if (cat == null || cat.isDead()) {
								this.cancel();
							}
							double diffX = vicLoc.getX() - loc.getX();
							double diffY = vicLoc.getY() - loc.getY();
							double diffZ = vicLoc.getZ() - loc.getZ();

							cat.setVelocity(new Vector(diffX, diffY, diffZ).normalize());

							if (cat.getLocation().distance(vicLoc) < .5) {
								cat.getWorld().createExplosion(cat.getLocation(), 5);
								PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.EXPLOSION_HUGE, false,
										tar2.getLocation().getBlockX(), tar2.getLocation().getBlockY()+1, tar2.getLocation().getBlockZ(), 0, 0, 0, 5, 1, null
										);
								((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
								for (Entity ee : p.getNearbyEntities(64, 64, 64)) {
									if(ee instanceof Player) {
										Player eee = (Player)ee;
										((CraftPlayer) eee).getHandle().playerConnection.sendPacket(packet);
									}
								}
								tar2.setFireTicks(20);
								// this.cancel();
							}

						}
					}.runTaskTimer(this, 0, 1);
					new BukkitRunnable() {
						public void run() {
							if (!cat.isDead()) {
								cat.remove();
							}
						}
					}.runTaskLater(this, 200);
				}

			}

		}
	}

	@EventHandler
	public void onExplode(BlockExplodeEvent e) {
		e.setCancelled(true);
	}
}
