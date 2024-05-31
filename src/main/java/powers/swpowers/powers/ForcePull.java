package powers.swpowers.powers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import powers.swpowers.SWPowers;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ForcePull {
    private final SWPowers plugin;
    private final HashMap<UUID, Long> lastForcePull;

    public ForcePull(SWPowers plugin, HashMap<UUID, Long> lastForcePull) {
        this.plugin = plugin;
        this.lastForcePull = lastForcePull;
    }

    public void execute(Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        // Check for cooldown
        if (lastForcePull.containsKey(playerId)) {
            long timeElapsed = (currentTime - lastForcePull.get(playerId)) / 1000;
            if (timeElapsed < 15) {
                int timeLeft = 15 - (int) timeElapsed;
                player.sendMessage(ChatColor.DARK_GREEN + "Force Pull is on " + timeLeft + " seconds cooldown.");
                return;
            }
        }

        // Apply Force Pull effect
        List<Entity> nearbyEntities = player.getNearbyEntities(2, 2, 2);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity target = (LivingEntity) entity;
                Vector direction = player.getLocation().toVector().subtract(target.getLocation().toVector()).normalize();
                target.setVelocity(direction.multiply(1)); // Adjust the multiplier for strength

                break; // Apply to the first found entity
            }
        }

        // Update last use time
        lastForcePull.put(playerId, currentTime);
    }
}
