package de.dan.homes.commands;

import de.dan.homes.config.HomeLimitService;
import de.dan.homes.storage.Home;
import de.dan.homes.storage.HomeManager;
import de.dan.homes.util.HomeNames;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class SetHomeCommand implements CommandExecutor {

    private final HomeManager homeManager;
    private final HomeLimitService limitService;

    public SetHomeCommand(HomeManager homeManager, HomeLimitService limitService) {
        this.homeManager = homeManager;
        this.limitService = limitService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /sethome <name>");
            return true;
        }

        String name = args[0];
        String error = HomeNames.validate(name);
        if (error != null) {
            player.sendMessage(ChatColor.RED + error);
            return true;
        }

        UUID uuid = player.getUniqueId();
        Integer existingSlot = homeManager.findSlotByName(uuid, name);
        if (existingSlot != null) {
            homeManager.setHomeAtSlot(player, existingSlot, name, player.getLocation());
            player.sendMessage(ChatColor.GREEN + "Your home " + ChatColor.YELLOW + name
                    + ChatColor.GREEN + " has been updated.");
            return true;
        }

        int maxHomes = limitService.getMaxHomes(player);
        Map<Integer, Home> homes = homeManager.getHomesBySlot(uuid);
        int freeSlot = -1;
        for (int i = 0; i < maxHomes; i++) {
            if (!homes.containsKey(i)) {
                freeSlot = i;
                break;
            }
        }

        if (freeSlot == -1) {
            player.sendMessage(ChatColor.RED + "You have already reached your limit of " + ChatColor.YELLOW
                    + maxHomes + ChatColor.RED + " home(s). Delete one first with /delhome <name>"
                    + " or manage your homes with /homes.");
            return true;
        }

        homeManager.setHomeAtSlot(player, freeSlot, name, player.getLocation());
        player.sendMessage(ChatColor.GREEN + "Home " + ChatColor.YELLOW + name
                + ChatColor.GREEN + " has been set.");
        return true;
    }
}
