package staff.swpowers;

import org.bukkit.plugin.java.JavaPlugin;

public final class SWPowers extends JavaPlugin {

    @Override
    public void onEnable() {
        // Pass 'this' to the event listener
        getServer().getPluginManager().registerEvents(new ForcePowerListener(this), this);

        // Register the '/powers' command
        getCommand("powers").setExecutor(new ForcePowersCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}