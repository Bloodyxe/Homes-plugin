package de.dan.homes;

import de.dan.homes.commands.DelHomeCommand;
import de.dan.homes.commands.HomeCommand;
import de.dan.homes.commands.HomesCommand;
import de.dan.homes.commands.SetHomeCommand;
import de.dan.homes.config.HomeLimitService;
import de.dan.homes.gui.HomeNameChatListener;
import de.dan.homes.gui.HomesGuiListener;
import de.dan.homes.gui.PendingHomeCreations;
import de.dan.homes.storage.HomeManager;
import de.dan.homes.teleport.HomeTeleportChannel;
import de.dan.homes.teleport.TeleportChannelListener;
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
        HomeTeleportChannel teleportChannel = new HomeTeleportChannel();

        getCommand("sethome").setExecutor(new SetHomeCommand(homeManager, limitService));
        getCommand("delhome").setExecutor(new DelHomeCommand(homeManager));

        HomeCommand homeCommand = new HomeCommand(this, homeManager, teleportChannel);
        getCommand("home").setExecutor(homeCommand);
        getCommand("home").setTabCompleter(homeCommand);

        HomesCommand homesCommand = new HomesCommand(this, homeManager, limitService, teleportChannel);
        getCommand("homes").setExecutor(homesCommand);
        getCommand("homes").setTabCompleter(homesCommand);

        getServer().getPluginManager().registerEvents(
                new HomesGuiListener(this, homeManager, limitService, pendingHomeCreations, teleportChannel), this);
        getServer().getPluginManager().registerEvents(
                new HomeNameChatListener(this, homeManager, pendingHomeCreations), this);
        getServer().getPluginManager().registerEvents(new TeleportChannelListener(teleportChannel), this);

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
