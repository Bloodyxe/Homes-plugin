package de.dan.homes.gui;

import de.dan.homes.HomesPlugin;
import de.dan.homes.storage.HomeManager;
import de.dan.homes.util.HomeNames;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Captures the next chat message of a player who just clicked an empty home
 * slot in the GUI, and uses it as that slot's new home name instead of
 * letting it reach public chat.
 *
 * Uses the classic AsyncPlayerChatEvent, which Paper still fires for plain
 * vanilla chat. If you run a chat-formatting plugin that fully replaces
 * chat handling (and stops firing this event), switch this listener to
 * Paper's io.papermc.paper.event.player.AsyncChatEvent instead - the logic
 * here stays the same, only the event type and event.message() change.
 */
public class HomeNameChatListener implements Listener {

    private final HomesPlugin plugin;
    private final HomeManager homeManager;
    private final PendingHomeCreations pendingHomeCreations;

    public HomeNameChatListener(HomesPlugin plugin, HomeManager homeManager,
                                 PendingHomeCreations pendingHomeCreations) {
        this.plugin = plugin;
        this.homeManager = homeManager;
        this.pendingHomeCreations = pendingHomeCreations;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!pendingHomeCreations.isPending(player.getUniqueId())) {
            return;
        }
        event.setCancelled(true);
        String message = event.getMessage().trim();
        // Location/inventory access and the home file both need the main
        // thread, but chat events fire off it.
        Bukkit.getScheduler().runTask(plugin, () -> handleInput(player, message));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        pendingHomeCreations.cancel(event.getPlayer().getUniqueId());
    }

    private void handleInput(Player player, String message) {
        UUID uuid = player.getUniqueId();
        Integer slot = pendingHomeCreations.consume(uuid);
        if (slot == null) {
            return;
        }

        if (message.equalsIgnoreCase("cancel")) {
            player.sendMessage(ChatColor.YELLOW + "Home creation cancelled.");
            return;
        }

        String error = HomeNames.validate(message);
        if (error != null) {
            player.sendMessage(ChatColor.RED + error);
            return;
        }

        if (homeManager.hasHomeNamed(uuid, message)) {
            player.sendMessage(ChatColor.RED + "You already have a home with that name.");
            return;
        }

        homeManager.setHomeAtSlot(player, slot, message, player.getLocation());
        player.sendMessage(ChatColor.GREEN + "Home " + ChatColor.YELLOW + message
                + ChatColor.GREEN + " has been set.");
    }
}
