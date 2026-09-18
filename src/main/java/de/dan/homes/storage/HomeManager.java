package de.dan.homes.storage;

import de.dan.homes.HomesPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Manages the homes of all players. Every player gets their own file
 * (homes/<uuid>.yml), so homes are strictly separated per player. There is
 * no way through the commands or the GUI to reach another player's homes,
 * since every access always goes through the UUID of the executing player.
 *
 * Homes are stored per fixed "slot" (0, 1, 2, ...) rather than only by name.
 * The slot a home occupies is what the GUI shows as a fixed box - this is
 * what lets the GUI always display exactly max-homes boxes, each either
 * showing a home's name or a "click to sethome" placeholder.
 */
public class HomeManager {

    private final HomesPlugin plugin;
    private final File homesFolder;
    private final Map<UUID, FileConfiguration> cache = new LinkedHashMap<>();
    private final Map<UUID, File> files = new LinkedHashMap<>();

    public HomeManager(HomesPlugin plugin) {
        this.plugin = plugin;
        this.homesFolder = new File(plugin.getDataFolder(), "homes");
        if (!homesFolder.exists()) {
            homesFolder.mkdirs();
        }
    }

    private FileConfiguration getConfig(UUID uuid) {
        return cache.computeIfAbsent(uuid, id -> {
            File file = getFile(id);
            return YamlConfiguration.loadConfiguration(file);
        });
    }

    private File getFile(UUID uuid) {
        return files.computeIfAbsent(uuid, id -> new File(homesFolder, id.toString() + ".yml"));
    }

    /**
     * Returns all of a player's homes, keyed by their slot number. Only
     * occupied slots are included.
     */
    public Map<Integer, Home> getHomesBySlot(UUID uuid) {
        FileConfiguration config = getConfig(uuid);
        Map<Integer, Home> homes = new LinkedHashMap<>();
        if (!config.isConfigurationSection("homes")) {
            return homes;
        }
        for (String key : config.getConfigurationSection("homes").getKeys(false)) {
            int slot;
            try {
                slot = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                continue;
            }

            String path = "homes." + key;
            String worldName = config.getString(path + ".world");
            if (worldName == null) {
                continue;
            }
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                // World is currently not loaded - skip this entry to avoid
                // NPEs. The file itself stays untouched.
                continue;
            }
            double x = config.getDouble(path + ".x");
            double y = config.getDouble(path + ".y");
            double z = config.getDouble(path + ".z");
            float yaw = (float) config.getDouble(path + ".yaw");
            float pitch = (float) config.getDouble(path + ".pitch");
            String name = config.getString(path + ".name", "Home " + (slot + 1));
            homes.put(slot, new Home(name, new Location(world, x, y, z, yaw, pitch)));
        }
        return Collections.unmodifiableMap(homes);
    }

    public Home getHomeAtSlot(UUID uuid, int slot) {
        return getHomesBySlot(uuid).get(slot);
    }

    public boolean isSlotOccupied(UUID uuid, int slot) {
        return getHomesBySlot(uuid).containsKey(slot);
    }

    /**
     * Finds the slot holding a home with the given name (case-insensitive).
     * Returns null if the player has no home with that name.
     */
    public Integer findSlotByName(UUID uuid, String name) {
        for (Map.Entry<Integer, Home> entry : getHomesBySlot(uuid).entrySet()) {
            if (entry.getValue().getName().equalsIgnoreCase(name)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public boolean hasHomeNamed(UUID uuid, String name) {
        return findSlotByName(uuid, name) != null;
    }

    public int countHomes(UUID uuid) {
        return getHomesBySlot(uuid).size();
    }

    /**
     * Sets (or overwrites) the home in the given slot for the given player,
     * at their current position.
     */
    public void setHomeAtSlot(Player player, int slot, String name, Location location) {
        UUID uuid = player.getUniqueId();
        FileConfiguration config = getConfig(uuid);
        String path = "homes." + slot;
        config.set(path + ".name", name);
        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", (double) location.getYaw());
        config.set(path + ".pitch", (double) location.getPitch());
        save(uuid);
    }

    /**
     * Clears a slot. Returns true if that slot was occupied.
     */
    public boolean deleteHomeAtSlot(UUID uuid, int slot) {
        FileConfiguration config = getConfig(uuid);
        String path = "homes." + slot;
        if (config.get(path) == null) {
            return false;
        }
        config.set(path, null);
        save(uuid);
        return true;
    }

    private void save(UUID uuid) {
        try {
            getConfig(uuid).save(getFile(uuid));
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save homes file for " + uuid + ".", e);
        }
    }

    public void saveAll() {
        for (UUID uuid : cache.keySet()) {
            save(uuid);
        }
    }
}
