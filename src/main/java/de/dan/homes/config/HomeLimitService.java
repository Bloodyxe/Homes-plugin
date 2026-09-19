package de.dan.homes.config;

import de.dan.homes.HomesPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/**
 * Determines how many homes a player is allowed to have. The values come
 * from config.yml and are driven by plain Bukkit permissions - meaning they
 * can be assigned 1:1 with LuckPerms (or any other permissions plugin) per
 * rank, without needing to change anything in the plugin itself.
 *
 * The HIGHEST limit the player has a permission for always wins - the
 * values are not additive. If a player doesn't have any of the permissions
 * listed under "permission-limits", "default-homes" applies.
 *
 * "permission-limits" is a LIST of {permission, limit} entries rather than
 * a map keyed by the permission node. That's deliberate: Bukkit's config
 * API treats "." in a map KEY as a path separator and silently splits it
 * into nested sections (so a key like "homes.limit.wonder" would never be
 * read back as one piece), but a "." inside a plain VALUE is just text -
 * so storing the permission node as a value sidesteps that entirely.
 */
public class HomeLimitService {

    private final HomesPlugin plugin;

    public HomeLimitService(HomesPlugin plugin) {
        this.plugin = plugin;
    }

    public int getMaxHomes(Player player) {
        FileConfiguration config = plugin.getConfig();
        int max = Math.max(0, config.getInt("default-homes", 3));

        for (Map<?, ?> entry : config.getMapList("permission-limits")) {
            String permission = asString(entry.get("permission"));
            Integer limit = asInt(entry.get("limit"));
            if (permission == null || limit == null) {
                continue;
            }
            if (!player.hasPermission(permission)) {
                continue;
            }
            if (limit > max) {
                max = limit;
            }
        }
        return max;
    }

    /**
     * The highest home limit anyone on the server could possibly have
     * (highest of "default-homes" and every "limit" under
     * "permission-limits"), regardless of what a specific player has. Used
     * purely for rendering: the GUI shows this many boxes in total, with
     * the ones beyond a given player's own limit shown as locked.
     */
    public int getHighestConfiguredLimit() {
        FileConfiguration config = plugin.getConfig();
        int max = Math.max(0, config.getInt("default-homes", 3));

        List<Map<?, ?>> entries = config.getMapList("permission-limits");
        for (Map<?, ?> entry : entries) {
            Integer limit = asInt(entry.get("limit"));
            if (limit != null && limit > max) {
                max = limit;
            }
        }
        return max;
    }

    private static String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private static Integer asInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return null;
    }
}
