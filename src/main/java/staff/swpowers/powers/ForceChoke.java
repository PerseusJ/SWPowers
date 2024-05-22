package staff.swpowers.powers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import staff.swpowers.SWPowers;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ForceChoke {
    private final SWPowers plugin;
    private final HashMap<UUID, Long> lastForceChoke;
    private final HashMap<UUID, UUID> activeForceChokes;

    public ForceChoke(SWPowers plugin, HashMap<UUID, Long> lastForceChoke, HashMap<UUID, UUID> activeForceChokes) {
        this.plugin = plugin;
        this.lastForceChoke = lastForceChoke;
        this.activeForceChokes = activeForceChokes;
    }

    public void execute(Player user) {
        UUID userId = user.getUniqueId();
        long currentTime = System.currentTimeMillis();

        // Check for cooldown
        if (lastForceChoke.containsKey(userId)) {
            long timeElapsed = (currentTime - lastForceChoke.get(userId)) / 1000;
            if (timeElapsed < 25) {
                int timeLeft = 25 - (int) timeElapsed;
                user.sendMessage(ChatColor.DARK_RED + "Force Choke is on " + timeLeft + " seconds cooldown.");
                return;
            }
        }

        // Apply Levitation and Poison effect to the first found living entity
        List<Entity> nearbyEntities = user.getNearbyEntities(5, 5, 5);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) entity;

                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 255)); // 80 ticks for 4 seconds
                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 1)); // 80 ticks for 4 seconds

                // Track the active Force Choke
                activeForceChokes.put(userId, target.getUniqueId());

                // Execute the "choke_hit" command on the target if it is a player
                if (target instanceof Player) {
                    Player targetPlayer = (Player) target;
                    targetPlayer.performCommand("emote choke_hit");
                }
                break; // Apply to the first found living entity
            }
        }

        // Schedule a task to remove the active Force Choke after 4 seconds
        new BukkitRunnable() {
            public void run() {
                activeForceChokes.remove(userId);
            }
        }.runTaskLater(plugin, 80); // 80 ticks for 4 seconds

        lastForceChoke.put(userId, currentTime);
        user.performCommand("emote choke");
    }
}
