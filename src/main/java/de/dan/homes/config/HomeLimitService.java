package de.dan.homes.config;

import de.dan.homes.HomesPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * Determines how many homes a player is allowed to have. The values come
 * from config.yml and are driven by plain Bukkit permissions - meaning they
 * can be assigned 1:1 with LuckPerms (or any other permissions plugin) per
 * rank, without needing to change anything in the plugin itself.
 *
 * The HIGHEST limit the player has a permission for always wins - the
 * values are not additive. If a player doesn't have any of the permissions
 * listed under "permission-limits", "default-homes" applies.
 */
public class HomeLimitService {

    private final HomesPlugin plugin;

    public HomeLimitService(HomesPlugin plugin) {
        this.plugin = plugin;
    }

    public int getMaxHomes(Player player) {
        FileConfiguration config = plugin.getConfig();
        int max = Math.max(0, config.getInt("default-homes", 3));

        ConfigurationSection section = config.getConfigurationSection("permission-limits");
        if (section != null) {
            for (String permission : section.getKeys(false)) {
                if (!player.hasPermission(permission)) {
                    continue;
                }
                int value = section.getInt(permission, 0);
                if (value > max) {
                    max = value;
                }
            }
        }
        return max;
    }
}
