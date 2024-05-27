package staff.swpowers.powers;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import staff.swpowers.SWPowers;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ForceRepulse {
    private final SWPowers plugin;
    private final HashMap<UUID, Long> lastForceRepulse;

    public ForceRepulse(SWPowers plugin, HashMap<UUID, Long> lastForceRepulse) {
        this.plugin = plugin;
        this.lastForceRepulse = lastForceRepulse;
    }

    public void execute(final Player player) {
        final UUID playerId = player.getUniqueId();
        final long currentTime = System.currentTimeMillis();
        final double allowableMovement = 0.25; // Allowable movement distance

        // Check if repulse is on cooldown
        if (lastForceRepulse.containsKey(playerId)) {
            long timeElapsed = (currentTime - lastForceRepulse.get(playerId)) / 1000;
            if (timeElapsed < 60) {
                int timeLeft = 60 - (int) timeElapsed;
                player.sendMessage(ChatColor.GOLD + "Force Repulse is on " + timeLeft + " seconds cooldown.");
                return;
            }
        }

        // Store the initial casting location with the block coordinates
        final Location initialLocation = player.getLocation().clone();
        final int initialX = initialLocation.getBlockX();
        final int initialY = initialLocation.getBlockY();
        final int initialZ = initialLocation.getBlockZ();
        spawnRepulseBuildupParticles(initialLocation.add(0, 1, 0)); // Spawn the buildup particles 1 block above

        new BukkitRunnable() {
            public void run() {
                // Get the current block location
                Location currentLocation = player.getLocation();
                int currentX = currentLocation.getBlockX();
                int currentY = currentLocation.getBlockY();
                int currentZ = currentLocation.getBlockZ();

                // Check if the player has moved from the initial block location
                if (initialX != currentX || initialY != currentY || initialZ != currentZ) {
                    player.sendMessage(ChatColor.RED + "Repulse casting interrupted due to movement.");
                    this.cancel(); // Cancel the repulse effect
                    return;
                }

                // Continue with the repulse effect
                List<Entity> nearbyEntities = player.getNearbyEntities(6, 6, 6);
                for (Entity entity : nearbyEntities) {
                    if (entity instanceof LivingEntity && entity != player) {
                        Vector direction = entity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                        entity.setVelocity(direction.multiply(10)); // Launch entities away
                    }
                }
                spawnRepulseReleaseParticles(player.getLocation()); // Spawn the release particles
            }
        }.runTaskLater(plugin, 40L); // 40 ticks = 2 second delay

        lastForceRepulse.put(playerId, currentTime); // Record the time when repulse was last used
        player.performCommand("emote repulse"); // Perform the repulse emote command
    }

    // Method to spawn initial buildup particles
    private void spawnRepulseBuildupParticles(Location center) {
        World world = center.getWorld();
        int particles = 150; // Adjust the number of particles to change the sphere's density
        double radius = 2.0; // The radius of the sphere
        center.setY(center.getY() + 1); // Shift the center up by 1 block

        for (int i = 0; i < particles; i++) {
            double phi = Math.acos(-1.0 + (2.0 * i) / particles);
            double theta = Math.sqrt(particles * Math.PI) * phi;

            for (int j = 0; j < 2; j++) { // This creates a full circle for each layer of the sphere
                double x = center.getX() + (radius * Math.sin(phi) * Math.cos(theta));
                double y = center.getY() + (radius * Math.cos(phi));
                double z = center.getZ() + (radius * Math.sin(phi) * Math.sin(theta));

                Location particleLocation = new Location(world, x, y, z);
                // Check if the location is not inside any block to avoid particles inside blocks
                if (!particleLocation.getBlock().getType().isSolid()) {
                    world.spawnParticle(Particle.END_ROD, particleLocation, 0, 0, 0, 0, 1);
                }
            }
        }
    }

    //Method to spawn release particles
    private void spawnRepulseReleaseParticles(Location location) {
        location.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, location, 20, 1, 1, 1, 0.1);
    }
}
