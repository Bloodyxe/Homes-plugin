package de.dan.homes.teleport;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class TeleportChannelListener implements Listener {

    private final HomeTeleportChannel channel;

    public TeleportChannelListener(HomeTeleportChannel channel) {
        this.channel = channel;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) {
            return;
        }
        channel.handleMove(event.getPlayer(), event.getFrom(), event.getTo());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        channel.cancelSilently(player.getUniqueId());
    }
}
