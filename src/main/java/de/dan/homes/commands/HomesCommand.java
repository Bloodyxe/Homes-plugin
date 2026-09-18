package de.dan.homes.commands;

import de.dan.homes.HomesPlugin;
import de.dan.homes.config.HomeLimitService;
import de.dan.homes.gui.HomesGui;
import de.dan.homes.storage.Home;
import de.dan.homes.storage.HomeManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * /homes           -> opens the click-GUI with one box per home slot
 * /homes <name>    -> teleports you directly to the given home
 * /homes reload    -> reloads config.yml (requires permission homes.admin)
 */
public class HomesCommand implements CommandExecutor, TabCompleter {

    private final HomesPlugin plugin;
    private final HomeManager homeManager;
    private final HomeLimitService limitService;

    public HomesCommand(HomesPlugin plugin, HomeManager homeManager, HomeLimitService limitService) {
        this.plugin = plugin;
        this.homeManager = homeManager;
        this.limitService = limitService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload") && sender.hasPermission("homes.admin")) {
            plugin.reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "config.yml has been reloaded.");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        if (args.length == 0) {
            HomesGui.open(plugin, homeManager, player, limitService.getMaxHomes(player));
            return true;
        }

        if (args.length == 1) {
            String name = args[0];
            Integer slot = homeManager.findSlotByName(player.getUniqueId(), name);
            Home home = slot == null ? null : homeManager.getHomeAtSlot(player.getUniqueId(), slot);
            if (home == null) {
                player.sendMessage(ChatColor.RED + "You don't have a home named " + ChatColor.YELLOW + name
                        + ChatColor.RED + ".");
                return true;
            }
            player.teleportAsync(home.getLocation());
            player.sendMessage(ChatColor.GREEN + "You have been teleported to " + ChatColor.YELLOW + home.getName()
                    + ChatColor.GREEN + ".");
            return true;
        }

        player.sendMessage(ChatColor.RED + "Usage: /homes [name]");
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
        if (sender.hasPermission("homes.admin") && "reload".startsWith(prefix)) {
            matches.add("reload");
        }
        return matches;
    }
}
