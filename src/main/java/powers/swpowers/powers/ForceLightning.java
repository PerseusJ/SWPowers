package powers.swpowers.powers;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import powers.swpowers.SWPowers;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class ForceLightning {
    private final SWPowers plugin;
    private final HashMap<UUID, Long> lastForceLightning;

    public ForceLightning(SWPowers plugin, HashMap<UUID, Long> lastForceLightning) {
        this.plugin = plugin;
        this.lastForceLightning = lastForceLightning;
    }

    public void execute(Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        if (lastForceLightning.containsKey(playerId)) {
            long timeElapsed = (currentTime - lastForceLightning.get(playerId)) / 1000;
            if (timeElapsed < 5) {
                int timeLeft = 5 - (int) timeElapsed;
                player.sendMessage(ChatColor.BLUE + "Force Lightning is on " + timeLeft + " seconds cooldown.");
                return;
            }
        }

        Entity target = getTargetEntity(player, 15); // Assuming 15 blocks is the max range
        if (target instanceof LivingEntity) {
            // Adjust the start location to the player's hand position
            Location handLocation = player.getLocation().add(0, 1.5, 0);
            handLocation.setYaw(player.getLocation().getYaw());
            handLocation.setPitch(player.getLocation().getPitch());
            Vector direction = handLocation.getDirection().normalize().multiply(0.5);
            handLocation.add(direction); // Starting from slightly in front of the player's hands

            createLightningParticles(handLocation, target.getLocation());
            ((LivingEntity) target).damage(3); // 1.5 hearts damage
        }

        lastForceLightning.put(playerId, currentTime);
        player.performCommand("emote lightning");
    }

    private Entity getTargetEntity(Player player, double range) {
        List<Entity> nearbyEntities = player.getNearbyEntities(range, range, range);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity && player.hasLineOfSight(entity) && entity.getLocation().distance(player.getLocation()) <= 10) {
                return entity;
            }
        }
        return null;
    }

    private void createLightningParticles(Location start, Location end) {
        World world = start.getWorld();
        int points = 20; // Increased number of points for more particles
        double space = 1.0 / points;

        Vector toTarget = end.toVector().subtract(start.toVector());
        Random random = new Random();

        for (int i = 0; i <= points; i++) {
            Location point = start.clone().add(toTarget.clone().multiply(space * i));
            point.add(random.nextDouble() * 0.5 - 0.25, random.nextDouble() * 0.5 - 0.25, random.nextDouble() * 0.5 - 0.25);

            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(0, 0, 255), 1); // Blue color
            world.spawnParticle(Particle.REDSTONE, point, 3, dustOptions); // Increased particle count
        }
    }
}
