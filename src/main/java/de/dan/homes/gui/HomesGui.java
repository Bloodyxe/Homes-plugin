package de.dan.homes.gui;

import de.dan.homes.HomesPlugin;
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
 * chest GUI where home slots sit in a checkerboard pattern starting on the
 * second row (row 1 is a plain top border). A box shows a red bed if that
 * home slot is occupied, or a "click to sethome" placeholder if it's still
 * free. No coordinates are ever shown - neither here nor in any chat
 * message.
 *
 * Only the homes of the given player are ever shown - a player can only
 * open this GUI for themself (see HomesCommand).
 */
public final class HomesGui {

    public static final int ROWS = 4;
    public static final int SIZE = ROWS * 9;

    // Columns (0-8) used for home boxes within a row - every other slot,
    // leaving a one-slot border on each side for a clean checkerboard look.
    private static final int[] HOME_COLUMNS = {1, 3, 5, 7};

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
        int perRow = HOME_COLUMNS.length;
        int row = FIRST_HOME_ROW + (homeIndex / perRow);
        if (row >= ROWS) {
            return -1;
        }
        int column = HOME_COLUMNS[homeIndex % perRow];
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
        if (row < FIRST_HOME_ROW) {
            return -1;
        }
        int columnIndex = -1;
        for (int i = 0; i < HOME_COLUMNS.length; i++) {
            if (HOME_COLUMNS[i] == column) {
                columnIndex = i;
                break;
            }
        }
        if (columnIndex == -1) {
            return -1;
        }
        return (row - FIRST_HOME_ROW) * HOME_COLUMNS.length + columnIndex;
    }

    public static void open(HomesPlugin plugin, HomeManager homeManager, Player player, int maxHomes) {
        Map<Integer, Home> homes = homeManager.getHomesBySlot(player.getUniqueId());

        HomesGuiHolder holder = new HomesGuiHolder(player.getUniqueId());
        Inventory inventory = plugin.getServer().createInventory(holder, SIZE, ChatColor.DARK_AQUA + "Your Homes");
        holder.setInventory(inventory);

        for (int rawSlot = 0; rawSlot < SIZE; rawSlot++) {
            int homeIndex = inventorySlotToHomeIndex(rawSlot);
            if (homeIndex == -1 || homeIndex >= maxHomes) {
                inventory.setItem(rawSlot, buildFillerItem());
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

    private static ItemStack buildFillerItem() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }
}
