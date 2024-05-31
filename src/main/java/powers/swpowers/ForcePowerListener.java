package powers.swpowers;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.GameMode;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;

import powers.swpowers.powers.*;

public class ForcePowerListener implements Listener {
    private final SWPowers plugin;
    private final HashMap<UUID, Boolean> forcePowersActive = new HashMap<>();
    private final HashMap<UUID, String> selectedPowers = new HashMap<>();
    private final HashMap<UUID, List<String>> powerSlots = new HashMap<>();
    private final HashMap<UUID, Long> lastPowerToggle = new HashMap<>();

    private final ForcePush forcePush;
    private final ForceMeditate forceMeditate;
    private final ForceLevitate forceLevitate;
    private final ForceFreeze forceFreeze;
    private final ForceChoke forceChoke;
    private final ForcePull forcePull;
    private final ForceMalacia forceMalacia;
    private final DarkAura darkAura;
    private final ForceRepulse forceRepulse;
    private final ForceLightning forceLightning;
    private final ElectricJudgement electricJudgement;

    private final HashMap<UUID, Long> lastForcePush = new HashMap<>();
    private final HashMap<UUID, Long> lastForceMeditate = new HashMap<>();
    private final HashMap<UUID, Long> lastForceLevitate = new HashMap<>();
    private final HashMap<UUID, Long> lastForceFreeze = new HashMap<>();
    private final HashMap<UUID, Long> lastForceChoke = new HashMap<>();
    private final HashMap<UUID, UUID> activeForceChokes = new HashMap<>();
    private final HashMap<UUID, Long> lastForcePull = new HashMap<>();
    private final HashMap<UUID, Long> lastMalacia = new HashMap<>();
    private final HashMap<UUID, Long> lastDarkAura = new HashMap<>();
    private final HashMap<UUID, Long> lastForceRepulse = new HashMap<>();
    private final HashMap<UUID, Long> lastForceLightning = new HashMap<>();
    private final HashMap<UUID, Long> lastElectricJudgement = new HashMap<>();
    private final HashMap<UUID, Long> lastDoubleJump = new HashMap<>();

    // Constructor to set the plugin instance and initialize force power instances
    public ForcePowerListener(SWPowers plugin) {
        this.plugin = plugin;
        this.forcePush = new ForcePush(plugin, lastForcePush);
        this.forceMeditate = new ForceMeditate(plugin, lastForceMeditate);
        this.forceLevitate = new ForceLevitate(plugin, lastForceLevitate);
        this.forceFreeze = new ForceFreeze(plugin, lastForceFreeze);
        this.forceChoke = new ForceChoke(plugin, lastForceChoke, activeForceChokes);
        this.forcePull = new ForcePull(plugin, lastForcePull);
        this.forceMalacia = new ForceMalacia(plugin, lastMalacia);
        this.darkAura = new DarkAura(plugin, lastDarkAura);
        this.forceRepulse = new ForceRepulse(plugin, lastForceRepulse);
        this.forceLightning = new ForceLightning(plugin, lastForceLightning);
        this.electricJudgement = new ElectricJudgement(plugin, lastElectricJudgement);
    }

    public void updateForcePowerScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("forcePowers", "dummy", ChatColor.GREEN + "Force Powers");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<String> playerPowers = powerSlots.getOrDefault(player.getUniqueId(), new ArrayList<>());
        for (int i = 0; i < playerPowers.size(); i++) {
            String powerName = playerPowers.get(i) == null ? "None" : playerPowers.get(i);
            Score score = objective.getScore(ChatColor.WHITE + "Slot " + (i + 1) + ": " + ChatColor.YELLOW + powerName);
            score.setScore(playerPowers.size() - i);
        }

        player.setScoreboard(scoreboard);
    }

    // Method to clear the scoreboard
    private void clearScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                && player.isSneaking() && player.getInventory().getItemInMainHand().getType() == Material.AIR) {
            // Check if the player has activated/deactivated powers recently
            long currentTime = System.currentTimeMillis();
            long lastToggleTime = lastPowerToggle.getOrDefault(playerId, 0L);

            if (currentTime - lastToggleTime > 500) { // Add a 500ms (0.5 second) delay
                // Toggle the force power state
                boolean wasActive = forcePowersActive.getOrDefault(playerId, false);
                forcePowersActive.put(playerId, !wasActive);

                // Update scoreboard visibility based on force power state
                if (wasActive) {
                    // If it was active before, hide the scoreboard as we are deactivating
                    player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                } else {
                    // If it was not active before, show the scoreboard as we are activating
                    updateForcePowerScoreboard(player);
                }

                // Provide feedback to the player
                player.sendMessage(ChatColor.YELLOW + "Force power " + (wasActive ? "deactivated" : "activated"));

                // Update the last power toggle time
                lastPowerToggle.put(playerId, currentTime);
            }

            event.setCancelled(true); // Cancel the event to prevent any unintended interactions
        }

        if (forcePowersActive.getOrDefault(playerId, false) && event.getAction().equals(Action.LEFT_CLICK_AIR)) {
            int currentSlot = player.getInventory().getHeldItemSlot();
            List<String> playerPowers = powerSlots.getOrDefault(playerId, new ArrayList<>());

            if (currentSlot < playerPowers.size()) {
                String selectedPower = playerPowers.get(currentSlot);
                if (selectedPower != null) {
                    switch (selectedPower) {
                        case "Force Push":
                            if (player.hasPermission("powers.forcepush")) {
                                forcePush.execute(player);
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have permission to use Force Push.");
                            }
                            break;
                        case "Force Meditate":
                            if (player.hasPermission("powers.forcemeditate")) {
                                forceMeditate.execute(player);
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have permission to use Force Meditate.");
                            }
                            break;
                        case "Force Levitate":
                            if (player.hasPermission("powers.forcelevitate")) {
                                forceLevitate.execute(player);
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have permission to use Force Levitate.");
                            }
                            break;
                        case "Force Freeze":
                            if (player.hasPermission("powers.forcefreeze")) {
                                forceFreeze.execute(player);
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have permission to use Force Freeze.");
                            }
                            break;
                        case "Force Choke":
                            if (player.hasPermission("powers.forcechoke")) {
                                forceChoke.execute(player);
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have permission to use Force Choke.");
                            }
                            break;
                        case "Force Pull":
                            if (player.hasPermission("powers.forcepull")) {
                                forcePull.execute(player);
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have permission to use Force Pull.");
                            }
                            break;
                        case "Force Malacia":
                            if (player.hasPermission("powers.forcemalacia")) {
                                forceMalacia.execute(player);
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have permission to use Force Malacia.");
                            }
                            break;
                        case "Dark Aura":
                            if (player.hasPermission("powers.darkaura")) {
                                darkAura.execute(player);
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have permission to use Dark Aura.");
                            }
                            break;
                        case "Force Repulse":
                            if (player.hasPermission("powers.forcerepulse")) {
                                forceRepulse.execute(player);
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have permission to use Force Repulse.");
                            }
                            break;
                        case "Force Lightning":
                            if (player.hasPermission("powers.forcelightning")) {
                                forceLightning.execute(player);
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have permission to use Force Lightning.");
                            }
                            break;
                        case "Electric Judgement":
                            if (player.hasPermission("powers.electricjudgement")) {
                                electricJudgement.execute(player);
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have permission to use Electric Judgement.");
                            }
                            break;
                    }
                    event.setCancelled(true); // Cancel the event to prevent any unintended interactions
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        Player p = e.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            // If the player is in CREATIVE or SPECTATOR mode, do not interfere with flight.
            return;
        }
        if (p.hasPotionEffect(PotionEffectType.SLOW)) {
            return;
        }
        if (forcePowersActive.getOrDefault(p.getUniqueId(), false) && p.hasPermission("powers.forcejump")) {
            List<String> playerPowers = powerSlots.getOrDefault(p.getUniqueId(), new ArrayList<>());
            int currentSlot = p.getInventory().getHeldItemSlot();
            String selectedPower = currentSlot < playerPowers.size() ? playerPowers.get(currentSlot) : null;
            if ("Force Jump".equals(selectedPower)) {
                if (p.getLocation().subtract(0, 1, 0).getBlock().getType() != Material.AIR && !p.isFlying()) {
                    p.setAllowFlight(true);
                }
            } else {
                p.setAllowFlight(false); // Disable flight when force jump is not selected
            }
        } else {
            p.setAllowFlight(false); // Disable flight when force powers are not active
            // Reset double jump cooldown when the player lands
            if (player.isOnGround() && lastDoubleJump.containsKey(player.getUniqueId())) {
                lastDoubleJump.remove(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onPlayerFly(PlayerToggleFlightEvent e) {
        Player player = e.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            // If the player is in CREATIVE or SPECTATOR mode, do not interfere with flight.
            return;
        }

        if (!player.hasPermission("powers.forcejump")) {
            return;
        }

        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        // Check for cooldown
        if (lastDoubleJump.containsKey(playerId)) {
            long timeElapsed = (currentTime - lastDoubleJump.get(playerId)) / 1000;
            if (timeElapsed < 4) { // 4-second cooldown
                player.sendMessage(ChatColor.RED + "Double Jump is on cooldown.");
                e.setCancelled(true);
                return;
            }
        }

        // Perform the double jump
        e.setCancelled(true);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setVelocity(player.getLocation().getDirection().multiply(1.5).setY(1));

        // Update last use time
        lastDoubleJump.put(playerId, currentTime);

        // Execute the command associated with double jump as the player
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.performCommand("emote roll");
        }, 2L); // 2 ticks delay
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                // Check if the player has the permission for Double Jump
                if (p.hasPermission("powers.forcejump")) {
                    // Cancel the fall damage if the player has the permission
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            UUID playerId = player.getUniqueId();
            int currentSlot = player.getInventory().getHeldItemSlot();

            List<String> playerPowers = powerSlots.getOrDefault(playerId, new ArrayList<>());
            Boolean isPowerActive = forcePowersActive.getOrDefault(playerId, false);
            if (currentSlot < playerPowers.size() && playerPowers.get(currentSlot) != null && isPowerActive) {
                String selectedPower = playerPowers.get(currentSlot);
                switch (selectedPower) {
                    case "Force Push":
                        forcePush.execute(player);
                        break;
                    case "Force Pull":
                        forcePull.execute(player);
                        break;
                    case "Force Freeze":
                        forceFreeze.execute(player);
                        break;
                    case "Force Choke":
                        forceChoke.execute(player);
                        break;
                    case "Force Malacia":
                        forceMalacia.execute(player);
                        break;
                    case "Dark Aura":
                        darkAura.execute(player);
                        break;
                    case "Force Repulse":
                        forceRepulse.execute(player);
                        break;
                    case "Force Lightning":
                        forceLightning.execute(player);
                        break;
                    case "Electric Judgement":
                        electricJudgement.execute(player);
                        break;
                    // Other cases...
                }

                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerFall(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            UUID playerId = player.getUniqueId();

            if (activeForceChokes.containsKey(playerId)) {
                UUID targetId = activeForceChokes.get(playerId);
                Player target = Bukkit.getPlayer(targetId);

                if (target != null) {
                    target.removePotionEffect(PotionEffectType.LEVITATION);
                    target.removePotionEffect(PotionEffectType.POISON);
                }

                activeForceChokes.remove(playerId);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().startsWith("Select Your Power")) {
            event.setCancelled(true); // Cancel all events inside the GUI

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();
            UUID playerId = player.getUniqueId();

            if (clickedItem != null && clickedItem.hasItemMeta()) {
                String itemName = clickedItem.getItemMeta().getDisplayName();

                // Handle 'Next Page' click
                if (itemName.equals(ChatColor.GOLD + "Next Page")) {
                    String title = event.getView().getTitle();
                    if (title.endsWith("Page 1")) {
                        player.openInventory(ForcePowerGUI.getPowerSelectionInventory(player, 2));
                    } else if (title.endsWith("Page 2")) {
                        player.openInventory(ForcePowerGUI.getPowerSelectionInventory(player, 3));
                    }
                } else if (itemName.equals(ChatColor.GOLD + "Previous Page")) {
                    // Handle 'Previous Page' click
                    player.openInventory(ForcePowerGUI.getPowerSelectionInventory(player, 1));
                } else {

                    // Initialize the power list for the player if not present
                    powerSlots.putIfAbsent(playerId, new ArrayList<>(Collections.nCopies(5, null)));

                    // Check if a Force power is selected
                    if (itemName.equals(ChatColor.GREEN + "Force Push")) {
                        selectedPowers.put(playerId, "Force Push");
                        player.sendMessage(ChatColor.GREEN + "Force Push selected. Now select a slot.");
                    }
                    if (itemName.equals(ChatColor.BLUE + "Force Meditate")) {
                        selectedPowers.put(player.getUniqueId(), "Force Meditate");
                        player.sendMessage(ChatColor.BLUE + "Force Meditate selected. Now select a slot.");
                    }
                    if (itemName.equals(ChatColor.YELLOW + "Force Levitate")) {
                        selectedPowers.put(player.getUniqueId(), "Force Levitate");
                        player.sendMessage(ChatColor.YELLOW + "Force Levitate selected. Now select a slot.");
                    }
                    if (itemName.equals(ChatColor.AQUA + "Force Freeze")) {
                        selectedPowers.put(player.getUniqueId(), "Force Freeze");
                        player.sendMessage(ChatColor.AQUA + "Force Freeze selected. Now select a slot.");
                    }
                    if (itemName.equals(ChatColor.DARK_RED + "Force Choke")) {
                        selectedPowers.put(player.getUniqueId(), "Force Choke");
                        player.sendMessage(ChatColor.DARK_RED + "Force Choke selected. Now select a slot.");
                    }
                    if (itemName.equals(ChatColor.DARK_GREEN + "Force Pull")) {
                        selectedPowers.put(playerId, "Force Pull");
                        player.sendMessage(ChatColor.DARK_GREEN + "Force Pull selected. Now select a slot.");
                    }
                    if (itemName.equals(ChatColor.LIGHT_PURPLE + "Force Malacia")) {
                        selectedPowers.put(playerId, "Force Malacia");
                        player.sendMessage(ChatColor.LIGHT_PURPLE + "Force Malacia selected. Now select a slot.");
                        updateForcePowerScoreboard(player); // Update the scoreboard
                    }
                    if (itemName.equals(ChatColor.DARK_PURPLE + "Dark Aura")) {
                        selectedPowers.put(playerId, "Dark Aura");
                        player.sendMessage(ChatColor.DARK_PURPLE + "Dark Aura selected. Now select a slot.");
                        updateForcePowerScoreboard(player); // Update the scoreboard
                    }
                    if (itemName.equals(ChatColor.GOLD + "Force Repulse")) {
                        selectedPowers.put(playerId, "Force Repulse");
                        player.sendMessage(ChatColor.GOLD + "Force Repulse selected. Now select a slot.");
                        updateForcePowerScoreboard(player); // Update the scoreboard
                    }
                    if (itemName.equals(ChatColor.BLUE + "Force Lightning")) {
                        selectedPowers.put(playerId, "Force Lightning");
                        player.sendMessage(ChatColor.BLUE + "Force Lightning selected. Now select a slot.");
                        updateForcePowerScoreboard(player); // Update the scoreboard
                    }
                    if (itemName.equals(ChatColor.GOLD + "Electric Judgement")) {
                        selectedPowers.put(playerId, "Electric Judgement");
                        player.sendMessage(ChatColor.GOLD + "Electric Judgement selected. Now select a slot.");
                        updateForcePowerScoreboard(player); // Update the scoreboard
                    }
                    if (itemName.equals(ChatColor.WHITE + "Force Jump")) {
                        selectedPowers.put(playerId, "Force Jump");
                        player.sendMessage(ChatColor.WHITE + "Force Jump selected. Now select a slot.");
                        updateForcePowerScoreboard(player); // Update the scoreboard
                    }

                    // Check if a slot is selected
                    if (itemName.contains("Slot ")) {
                        int slot = Integer.parseInt(itemName.split(" ")[1]) - 1; // Convert to 0-based index
                        List<String> playerPowers = powerSlots.get(playerId);
                        playerPowers.set(slot, selectedPowers.get(playerId));
                        player.sendMessage(ChatColor.GREEN + "Power assigned to slot " + (slot + 1));

                        // Update the scoreboard
                        updateForcePowerScoreboard(player);

                        // Save the selected force powers for the player
                        saveSelectedForcePowers(player);
                    }
                }
            }
        }
    }

    private void saveSelectedForcePowers(Player player) {
        UUID playerId = player.getUniqueId();
        List<String> playerPowers = powerSlots.getOrDefault(playerId, new ArrayList<>());

        // Convert the list of force powers to a string
        String forcePowersString = String.join(",", playerPowers);

        // Save the force powers string to the player's persistent data
        player.setMetadata("selectedForcePowers", new FixedMetadataValue(plugin, forcePowersString));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Load the selected force powers for the player
        if (player.hasMetadata("selectedForcePowers")) {
            String forcePowersString = player.getMetadata("selectedForcePowers").get(0).asString();
            String[] forcePowers = forcePowersString.split(",");
            List<String> playerPowers = Arrays.asList(forcePowers);
            powerSlots.put(playerId, playerPowers);

            // Update the scoreboard
            updateForcePowerScoreboard(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Remove the selected force powers for the player
        powerSlots.remove(playerId);
    }
}