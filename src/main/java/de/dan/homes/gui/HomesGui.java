package de.dan.homes.gui;

import de.dan.homes.HomesPlugin;
import de.dan.homes.config.HomeLimitService;
import de.dan.homes.storage.Home;
import de.dan.homes.storage.HomeManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

/**
 * Builds and opens the main Homes overview GUI for a player: a fixed 4-row
 * chest GUI where home boxes sit in a brick/checkerboard pattern across
 * rows 2 and 3 (row 1 is a plain top border, row 4 a plain bottom border),
 * each row offset by one column from the row above it. A box shows a red
 * bed if that home slot is occupied, a green dye "click to sethome" if it's
 * free, or a lock if it's beyond the player's own limit but could be
 * unlocked with a higher rank (see HomeLimitService). No coordinates are
 * ever shown - neither here nor in any chat message.
 *
 * Only the homes of the given player are ever shown - a player can only
 * open this GUI for themself (see HomesCommand).
 */
public final class HomesGui {

    public static final int ROWS = 4;
    public static final int SIZE = ROWS * 9;

    // One pattern of columns (0-8) per home row, each offset from the
    // previous one by a single column - that's the brick/checkerboard look.
    // Row 1 (index 0 here) starts one column further right than before.
    private static final int[][] ROW_PATTERNS = {
            {2, 4, 6, 8},
            {1, 3, 5, 7},
            {2, 4, 6, 8},
    };

    // The top row (row 0) is a plain border; home boxes start on the
    // second row.
    private static final int FIRST_HOME_ROW = 1;

    private HomesGui() {
    }

    /**
     * Converts a home slot index (0, 1, 2, ... - what's stored in the
     * player's homes file) to the raw inventory slot it's displayed at.
     * Returns -1 if the index doesn't fit in the available rows.
     */
    public static int homeIndexToInventorySlot(int homeIndex) {
        int perRow = 4;
        int rowOffset = homeIndex / perRow;
        if (rowOffset >= ROW_PATTERNS.length) {
            return -1;
        }
        int row = FIRST_HOME_ROW + rowOffset;
        if (row >= ROWS) {
            return -1;
        }
        int column = ROW_PATTERNS[rowOffset][homeIndex % perRow];
        return row * 9 + column;
    }

    /**
     * Converts a raw inventory slot back to a home slot index, or -1 if
     * that slot isn't one of the checkerboard home boxes (e.g. it's part
     * of the border/filler).
     */
    public static int inventorySlotToHomeIndex(int rawSlot) {
        if (rawSlot < 0 || rawSlot >= SIZE) {
            return -1;
        }
        int row = rawSlot / 9;
        int column = rawSlot % 9;
        int rowOffset = row - FIRST_HOME_ROW;
        if (rowOffset < 0 || rowOffset >= ROW_PATTERNS.length) {
            return -1;
        }
        int[] pattern = ROW_PATTERNS[rowOffset];
        for (int i = 0; i < pattern.length; i++) {
            if (pattern[i] == column) {
                return rowOffset * pattern.length + i;
            }
        }
        return -1;
    }

    public static void open(HomesPlugin plugin, HomeManager homeManager, HomeLimitService limitService,
                             Player player) {
        int maxHomes = limitService.getMaxHomes(player);
        int highestConfiguredLimit = Math.max(maxHomes, limitService.getHighestConfiguredLimit());
        Map<Integer, Home> homes = homeManager.getHomesBySlot(player.getUniqueId());

        HomesGuiHolder holder = new HomesGuiHolder(player.getUniqueId());
        Inventory inventory = plugin.getServer().createInventory(holder, SIZE, ChatColor.DARK_AQUA + "Your Homes");
        holder.setInventory(inventory);

        for (int rawSlot = 0; rawSlot < SIZE; rawSlot++) {
            int homeIndex = inventorySlotToHomeIndex(rawSlot);
            if (homeIndex == -1) {
                inventory.setItem(rawSlot, buildFillerItem());
                continue;
            }
            if (homeIndex >= highestConfiguredLimit) {
                inventory.setItem(rawSlot, buildFillerItem());
                continue;
            }
            if (homeIndex >= maxHomes) {
                inventory.setItem(rawSlot, buildLockedItem());
                continue;
            }
            Home home = homes.get(homeIndex);
            inventory.setItem(rawSlot, home != null ? buildOccupiedItem(home) : buildEmptyItem());
        }

        player.openInventory(inventory);
    }

    private static ItemStack buildOccupiedItem(Home home) {
        ItemStack item = new ItemStack(Material.RED_BED);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + home.getName());
        meta.setLore(List.of(
                ChatColor.YELLOW + "Click to manage this home"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildEmptyItem() {
        ItemStack item = new ItemStack(Material.LIME_DYE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Click to sethome");
        meta.setLore(List.of(
                ChatColor.GRAY + "This home slot is empty.",
                ChatColor.GRAY + "Click here, then type a name",
                ChatColor.GRAY + "in chat to set your current",
                ChatColor.GRAY + "location as this home."
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildLockedItem() {
        ItemStack item = new ItemStack(Material.IRON_BARS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GRAY + "Locked");
        meta.setLore(List.of(
                ChatColor.GRAY + "This home slot isn't unlocked yet.",
                ChatColor.GRAY + "A higher rank on this server",
                ChatColor.GRAY + "grants extra home slots."
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildFillerItem() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }
}
