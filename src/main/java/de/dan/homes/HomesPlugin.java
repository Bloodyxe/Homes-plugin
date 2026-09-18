package de.dan.homes;

import de.dan.homes.commands.DelHomeCommand;
import de.dan.homes.commands.HomesCommand;
import de.dan.homes.commands.SetHomeCommand;
import de.dan.homes.config.HomeLimitService;
import de.dan.homes.gui.HomeNameChatListener;
import de.dan.homes.gui.HomesGuiListener;
import de.dan.homes.gui.PendingHomeCreations;
import de.dan.homes.storage.HomeManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class HomesPlugin extends JavaPlugin {

    private HomeManager homeManager;
    private HomeLimitService limitService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.homeManager = new HomeManager(this);
        this.limitService = new HomeLimitService(this);
        PendingHomeCreations pendingHomeCreations = new PendingHomeCreations();

        getCommand("sethome").setExecutor(new SetHomeCommand(homeManager, limitService));
        getCommand("delhome").setExecutor(new DelHomeCommand(homeManager));

        HomesCommand homesCommand = new HomesCommand(this, homeManager, limitService);
        getCommand("homes").setExecutor(homesCommand);
        getCommand("homes").setTabCompleter(homesCommand);

        getServer().getPluginManager().registerEvents(
                new HomesGuiListener(this, homeManager, limitService, pendingHomeCreations), this);
        getServer().getPluginManager().registerEvents(
                new HomeNameChatListener(this, homeManager, pendingHomeCreations), this);

        getLogger().info("HomesPlugin has been enabled.");
    }

    @Override
    public void onDisable() {
        if (homeManager != null) {
            homeManager.saveAll();
        }
        getLogger().info("HomesPlugin has been disabled.");
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }
}
