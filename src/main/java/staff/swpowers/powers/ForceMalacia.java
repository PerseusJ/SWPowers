package staff.swpowers.powers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import staff.swpowers.SWPowers;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ForceMalacia {
    private final SWPowers plugin;
    private final HashMap<UUID, Long> lastMalacia;

    public ForceMalacia(SWPowers plugin, HashMap<UUID, Long> lastMalacia) {
        this.plugin = plugin;
        this.lastMalacia = lastMalacia;
    }

    public void execute(Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        if (lastMalacia.containsKey(playerId)) {
            long timeElapsed = (currentTime - lastMalacia.get(playerId)) / 1000;
            if (timeElapsed < 30) {
                int timeLeft = 30 - (int) timeElapsed;
                player.sendMessage(ChatColor.DARK_PURPLE + "Force Malacia is on " + timeLeft + " seconds cooldown.");
                return;
            }
        }

        List<Entity> nearbyEntities = player.getNearbyEntities(5, 5, 5);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player) {
                Player target = (Player) entity;
                target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 1)); // 100 ticks for 5 seconds
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1));
            }
        }

        lastMalacia.put(playerId, currentTime);
    }
}
