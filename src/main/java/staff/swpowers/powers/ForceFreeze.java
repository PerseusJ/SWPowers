package staff.swpowers.powers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import staff.swpowers.SWPowers;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ForceFreeze {
    private final SWPowers plugin;
    private final HashMap<UUID, Long> lastForceFreeze;

    public ForceFreeze(SWPowers plugin, HashMap<UUID, Long> lastForceFreeze) {
        this.plugin = plugin;
        this.lastForceFreeze = lastForceFreeze;
    }

    public void execute(Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        // Check for cooldown
        if (lastForceFreeze.containsKey(playerId)) {
            long timeElapsed = (currentTime - lastForceFreeze.get(playerId)) / 1000;
            if (timeElapsed < 3) {
                int timeLeft = 3 - (int) timeElapsed;
                player.sendMessage(ChatColor.AQUA + "Force Freeze is on " + timeLeft + " seconds cooldown.");
                return;
            }
        }

        // Get nearby entities and apply freeze
        List<Entity> nearbyEntities = player.getNearbyEntities(5, 5, 5);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) entity;
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 255)); // 80 ticks for 4 seconds
                break; // Apply to the first found entity
            }
        }

        lastForceFreeze.put(playerId, currentTime);
    }
}
