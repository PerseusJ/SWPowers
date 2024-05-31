package powers.swpowers.powers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import powers.swpowers.SWPowers;

import java.util.HashMap;
import java.util.UUID;

public class ForceLevitate {
    private final SWPowers plugin;
    private final HashMap<UUID, Long> lastForceLevitate;

    public ForceLevitate(SWPowers plugin, HashMap<UUID, Long> lastForceLevitate) {
        this.plugin = plugin;
        this.lastForceLevitate = lastForceLevitate;
    }

    public void execute(final Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        // Check for cooldown
        if (lastForceLevitate.containsKey(playerId)) {
            long timeElapsed = (currentTime - lastForceLevitate.get(playerId)) / 1000;
            if (timeElapsed < 10) {
                int timeLeft = 10 - (int) timeElapsed;
                player.sendMessage(ChatColor.YELLOW + "Force Levitate is on " + timeLeft + " seconds cooldown.");
                return;
            }
        }

        // Apply Levitation effect
        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 80, 1)); // 80 ticks for 4 seconds, amplifier 1

        lastForceLevitate.put(playerId, currentTime);
    }
}
