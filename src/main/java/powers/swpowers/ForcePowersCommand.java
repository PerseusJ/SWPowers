package powers.swpowers;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import powers.swpowers.ForcePowerGUI;

public class ForcePowersCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Inventory powerSelectionInv = ForcePowerGUI.getPowerSelectionInventory(player,1); // Open first page
            player.openInventory(powerSelectionInv);

            player.sendMessage(ChatColor.YELLOW + "To activate and deactivate your powers, please shift and right-click with an empty hand.");

            return true;
        }
        return false;
    }
}