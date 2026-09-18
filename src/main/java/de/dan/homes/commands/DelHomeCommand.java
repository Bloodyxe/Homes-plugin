package de.dan.homes.commands;

import de.dan.homes.storage.HomeManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DelHomeCommand implements CommandExecutor {

    private final HomeManager homeManager;

    public DelHomeCommand(HomeManager homeManager) {
        this.homeManager = homeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /delhome <name>");
            return true;
        }

        String name = args[0];
        Integer slot = homeManager.findSlotByName(player.getUniqueId(), name);
        if (slot == null) {
            player.sendMessage(ChatColor.RED + "You don't have a home named " + ChatColor.YELLOW + name
                    + ChatColor.RED + ".");
            return true;
        }

        homeManager.deleteHomeAtSlot(player.getUniqueId(), slot);
        player.sendMessage(ChatColor.GREEN + "Home " + ChatColor.YELLOW + name
                + ChatColor.GREEN + " has been deleted.");
        return true;
    }
}
