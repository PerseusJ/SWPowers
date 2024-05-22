package staff.swpowers.powers;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import staff.swpowers.SWPowers;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class ElectricJudgement {
    private final SWPowers plugin;
    private final HashMap<UUID, Long> lastElectricJudgement;

    public ElectricJudgement(SWPowers plugin, HashMap<UUID, Long> lastElectricJudgement) {
        this.plugin = plugin;
        this.lastElectricJudgement = lastElectricJudgement;
    }

    public void execute(Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        if (lastElectricJudgement.containsKey(playerId)) {
            long timeElapsed = (currentTime - lastElectricJudgement.get(playerId)) / 1000;
            if (timeElapsed < 5) { // Adjust the cooldown period if different
                int timeLeft = 5 - (int) timeElapsed;
                player.sendMessage(ChatColor.GOLD + "Electric Judgement is on " + timeLeft + " seconds cooldown.");
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

            createOrangeLightningParticles(handLocation, target.getLocation());
            ((LivingEntity) target).damage(3); // 1.5 hearts damage
        }

        lastElectricJudgement.put(playerId, currentTime);
        player.performCommand("emote electric_judgement");
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

    private void createOrangeLightningParticles(Location start, Location end) {
        World world = start.getWorld();
        int points = 20; // Number of points along the line
        double space = 1.0 / points;

        Vector toTarget = end.toVector().subtract(start.toVector());
        Random random = new Random();

        for (int i = 0; i <= points; i++) {
            Location point = start.clone().add(toTarget.clone().multiply(space * i));
            point.add(random.nextDouble() * 0.5 - 0.25, random.nextDouble() * 0.5 - 0.25, random.nextDouble() * 0.5 - 0.25);

            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 165, 0), 1); // Orange color
            world.spawnParticle(Particle.REDSTONE, point, 3, dustOptions); // Increased particle count
        }
    }
}
