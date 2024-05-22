package staff.swpowers.powers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import staff.swpowers.SWPowers;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ForcePush {
    private final SWPowers plugin;
    private final HashMap<UUID, Long> lastForcePush;

    public ForcePush(SWPowers plugin, HashMap<UUID, Long> lastForcePush) {
        this.plugin = plugin;
        this.lastForcePush = lastForcePush;
    }

    public void execute(Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        // Check for cooldown
        if (lastForcePush.containsKey(playerId)) {
            long timeElapsed = (currentTime - lastForcePush.get(playerId)) / 1000;
            if (timeElapsed < 15) {
                int timeLeft = 15 - (int) timeElapsed;
                player.sendMessage(ChatColor.RED + "Force Push is on " + timeLeft + " seconds cooldown.");
                return;
            }
        }

        List<Entity> nearbyEntities = player.getNearbyEntities(5, 5, 5);
        for (Entity entity : nearbyEntities) {
            Vector direction = entity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
            entity.setVelocity(direction.multiply(2)); // Push entities away
        }

        // Update last use time
        lastForcePush.put(playerId, currentTime);
        player.performCommand("emote push");
    }
}