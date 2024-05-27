package staff.swpowers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.lone.itemsadder.api.CustomStack;

public class ForcePowerGUI {
    private static final String[] powers = {
            "staff.forcepush", "staff.forcemeditate", "staff.forcelevitate",
            "staff.forcefreeze", "staff.forcechoke", "staff.forcepull",
            "staff.forcemalacia", "staff.darkaura", "staff.forcerepulse",
            "staff.forcelightning", "staff.electricjudgement", "staff.forcejump"
    };

    private static final String[] customItems = {
            "tgbitems:crystalgreen", "tgbitems:crystalblue", "tgbitems:crystalyellow",
            "tgbitems:crystalcyan", "tgbitems:crystalred", "tgbitems:crystallime",
            "tgbitems:crystalpurple", "tgbitems:crystalmagenta", "tgbitems:crystalamber",
            "tgbitems:crystalblue", "tgbitems:crystalamber", "tgbitems:crystalwhite",
    };

    private static final String[] names = {
            ChatColor.GREEN + "Force Push", ChatColor.BLUE + "Force Meditate", ChatColor.YELLOW + "Force Levitate",
            ChatColor.AQUA + "Force Freeze", ChatColor.DARK_RED + "Force Choke", ChatColor.DARK_GREEN + "Force Pull",
            ChatColor.LIGHT_PURPLE + "Force Malacia", ChatColor.DARK_PURPLE + "Dark Aura", ChatColor.GOLD + "Force Repulse",
            ChatColor.BLUE + "Force Lightning", ChatColor.GOLD + "Electric Judgement", ChatColor.WHITE + "Force Jump"
    };

    public static Inventory getPowerSelectionInventory(Player player, int page) {
        Inventory inv = Bukkit.createInventory(null, 27, "Select Your Power - Page " + page);
        int slotIndex = 2; // Start from the third slot
        int powersAdded = 0;

        for (int i = 0; i < powers.length; i++) {
            if (player.hasPermission(powers[i]) || player.isOp()) {
                if (powersAdded >= (5 * (page - 1)) && powersAdded < (5 * page)) {
                    CustomStack customStack = CustomStack.getInstance(customItems[i]);
                    if (customStack != null) {
                        ItemStack item = customStack.getItemStack();
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName(names[i]);
                        item.setItemMeta(meta);
                        inv.setItem(slotIndex++, item);
                    }
                }
                powersAdded++;
            }
        }

        // Pagination
        if (powersAdded > 5 * page || page > 1) {
            if (page > 1) {
                ItemStack previousPage = new ItemStack(Material.ARROW);
                ItemMeta previousPageMeta = previousPage.getItemMeta();
                previousPageMeta.setDisplayName(ChatColor.GOLD + "Previous Page");
                previousPage.setItemMeta(previousPageMeta);
                inv.setItem(18, previousPage); // Bottom left corner
            }
            if (powersAdded > 5 * page) {
                ItemStack nextPage = new ItemStack(Material.ARROW);
                ItemMeta nextPageMeta = nextPage.getItemMeta();
                nextPageMeta.setDisplayName(ChatColor.GOLD + "Next Page");
                nextPage.setItemMeta(nextPageMeta);
                inv.setItem(26, nextPage); // Bottom right corner
            }
        }

        // Slot panels
        for (int i = 20; i < 25; i++) {
            ItemStack panel = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
            ItemMeta panelMeta = panel.getItemMeta();
            panelMeta.setDisplayName(ChatColor.WHITE + "Slot " + (i - 19));
            panel.setItemMeta(panelMeta);
            inv.setItem(i, panel);
        }

        return inv;
    }

    public static Inventory getPowerSelectionInventory(Player player) {
        return getPowerSelectionInventory(player, 1); // Default to page 1
    }
}
