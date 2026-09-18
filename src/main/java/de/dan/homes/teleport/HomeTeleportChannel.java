package de.dan.homes.teleport;

import de.dan.homes.HomesPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adds a 5 second "channel" before a home teleport happens: the player gets
 * warped only if they stand still for the whole duration. Moving (walking,
 * falling, being pushed - anything that changes their block position)
 * cancels the pending teleport. Used by /home, /homes <name> and the "TP to
 * home" GUI button, so the delay can't just be bypassed by using a
 * different one of those.
 */
public class HomeTeleportChannel {

    private static final long DELAY_TICKS = 5 * 20L; // 5 seconds

    private final Map<UUID, Pending> pending = new ConcurrentHashMap<>();

    private record Pending(Location startLocation, BukkitTask task) {
    }

    public boolean isPending(UUID uuid) {
        return pending.containsKey(uuid);
    }

    /**
     * Starts the 5 second channel for this player. Returns false (and does
     * nothing) if the player already has a teleport in progress.
     */
    public boolean start(HomesPlugin plugin, Player player, String homeName, Location destination) {
        UUID uuid = player.getUniqueId();
        if (pending.containsKey(uuid)) {
            return false;
        }

        Location startLocation = player.getLocation();
        player.sendMessage(ChatColor.YELLOW + "Teleporting to " + ChatColor.WHITE + homeName + ChatColor.YELLOW
                + " in 5 seconds. Don't move!");

        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            pending.remove(uuid);
            if (!player.isOnline()) {
                return;
            }
            player.teleportAsync(destination);
            player.sendMessage(ChatColor.GREEN + "You have been teleported to " + ChatColor.YELLOW + homeName
                    + ChatColor.GREEN + ".");
        }, DELAY_TICKS);

        pending.put(uuid, new Pending(startLocation, task));
        return true;
    }

    /**
     * Called on every player move; cancels that player's pending teleport
     * if they actually changed position (ignores pure camera look).
     */
    public void handleMove(Player player, Location from, Location to) {
        UUID uuid = player.getUniqueId();
        Pending current = pending.get(uuid);
        if (current == null) {
            return;
        }
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) {
            return; // just looked around, didn't actually move
        }
        current.task().cancel();
        pending.remove(uuid);
        player.sendMessage(ChatColor.RED + "Teleport cancelled because you moved.");
    }

    /**
     * Cancels a pending teleport silently (e.g. on disconnect), without
     * sending any message.
     */
    public void cancelSilently(UUID uuid) {
        Pending current = pending.remove(uuid);
        if (current != null) {
            current.task().cancel();
        }
    }
}
