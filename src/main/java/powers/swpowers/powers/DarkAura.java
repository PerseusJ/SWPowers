package powers.swpowers.powers;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import powers.swpowers.SWPowers;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DarkAura {
    private final SWPowers plugin;
    private final HashMap<UUID, Long> lastDarkAura;

    public DarkAura(SWPowers plugin, HashMap<UUID, Long> lastDarkAura) {
        this.plugin = plugin;
        this.lastDarkAura = lastDarkAura;
    }

    public void execute(Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        if (lastDarkAura.containsKey(playerId)) {
            long timeElapsed = (currentTime - lastDarkAura.get(playerId)) / 1000;
            if (timeElapsed < 45) {
                int timeLeft = 45 - (int) timeElapsed;
                player.sendMessage(ChatColor.DARK_PURPLE + "Dark Aura is on " + timeLeft + " seconds cooldown.");
                return;
            }
        }

        lastDarkAura.put(playerId, currentTime);

        new BukkitRunnable() {
            int duration = 5; // Duration of the aura in seconds

            public void run() {
                if (duration <= 0) {
                    this.cancel();
                    return;
                }

                player.getWorld().spawnParticle(Particle.DRAGON_BREATH, player.getLocation(), 100, 2.5, 0.5, 2.5, 0.05);

                List<Entity> nearbyEntities = player.getNearbyEntities(5, 5, 5);
                for (Entity entity : nearbyEntities) {
                    if (entity instanceof LivingEntity && entity != player) {
                        ((LivingEntity) entity).damage(3); // 1.5 hearts of damage
                    }
                }

                duration--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // 20 ticks = 1 second
    }
}
