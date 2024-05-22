package staff.swpowers.powers;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import staff.swpowers.SWPowers;

import java.util.HashMap;
import java.util.UUID;

public class ForceMeditate {
    private final SWPowers plugin;
    private final HashMap<UUID, Long> lastForceMeditate;

    public ForceMeditate(SWPowers plugin, HashMap<UUID, Long> lastForceMeditate) {
        this.plugin = plugin;
        this.lastForceMeditate = lastForceMeditate;
    }

    public void execute(final Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        // Check for cooldown
        if (lastForceMeditate.containsKey(playerId)) {
            long timeElapsed = (currentTime - lastForceMeditate.get(playerId)) / 1000;
            if (timeElapsed < 30) {
                int timeLeft = 30 - (int) timeElapsed;
                player.sendMessage(ChatColor.BLUE + "Force Meditate is on " + timeLeft + " seconds cooldown.");
                return;
            }
        }

        // Store the player's initial location for movement check
        Location initialLocation = player.getLocation();

        // Apply Regeneration effect for 30 seconds (600 ticks)
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 1));

        new BukkitRunnable() {
            public void run() {
                if (player.isOnline()) {
                    // Check if player has moved
                    if (!player.getLocation().getBlock().equals(initialLocation.getBlock())) {
                        player.sendMessage(ChatColor.RED + "Meditation interrupted due to movement.");
                        player.removePotionEffect(PotionEffectType.REGENERATION);
                        this.cancel();
                    } else {
                        // Check if meditation is complete
                        if (!player.hasPotionEffect(PotionEffectType.REGENERATION)) {
                            // Spawn healing light particles around the player
                            Location particleLocation = player.getLocation().add(0, 1, 0);
                            player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, particleLocation, 50, 0.5, 0.5, 0.5, 0.1);
                            this.cancel();
                        }
                    }
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 30); // Check every 1.5 seconds (30 ticks)

        lastForceMeditate.put(playerId, currentTime);
        player.performCommand("emote meditate");
    }
}
