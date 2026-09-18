package de.dan.homes.commands;

import de.dan.homes.HomesPlugin;
import de.dan.homes.storage.Home;
import de.dan.homes.storage.HomeManager;
import de.dan.homes.teleport.HomeTeleportChannel;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * /home <name> - direct shortcut to teleport to one of your homes, without
 * going through the /homes GUI. Teleports after a 5 second channel during
 * which you must not move (see HomeTeleportChannel), same as /homes <name>
 * and the "TP to home" GUI button - so the delay can't be sidestepped by
 * using a different command.
 */
public class HomeCommand implements CommandExecutor, TabCompleter {

    private final HomesPlugin plugin;
    private final HomeManager homeManager;
    private final HomeTeleportChannel teleportChannel;

    public HomeCommand(HomesPlugin plugin, HomeManager homeManager, HomeTeleportChannel teleportChannel) {
        this.plugin = plugin;
        this.homeManager = homeManager;
        this.teleportChannel = teleportChannel;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /home <name>");
            return true;
        }

        String name = args[0];
        Integer slot = homeManager.findSlotByName(player.getUniqueId(), name);
        Home home = slot == null ? null : homeManager.getHomeAtSlot(player.getUniqueId(), slot);
        if (home == null) {
            player.sendMessage(ChatColor.RED + "You don't have a home named " + ChatColor.YELLOW + name
                    + ChatColor.RED + ".");
            return true;
        }

        if (!teleportChannel.start(plugin, player, home.getName(), home.getLocation())) {
            player.sendMessage(ChatColor.RED + "You are already teleporting. Please wait.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player) || args.length != 1) {
            return List.of();
        }
        String prefix = args[0].toLowerCase();
        List<String> matches = new ArrayList<>();
        for (Home home : homeManager.getHomesBySlot(player.getUniqueId()).values()) {
            if (home.getName().toLowerCase().startsWith(prefix)) {
                matches.add(home.getName());
            }
        }
        return matches;
    }
}
