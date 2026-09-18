package de.dan.homes.gui;

import de.dan.homes.HomesPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * The small "what do you want to do with this home" menu that opens after
 * clicking an occupied slot in the main Homes GUI.
 */
public final class HomeDetailGui {

    private static final int SIZE = 27;
    public static final int SLOT_TELEPORT = 11;
    public static final int SLOT_DELETE = 15;
    public static final int SLOT_BACK = 22;

    private HomeDetailGui() {
    }

    public static void open(HomesPlugin plugin, Player player, int slot, String homeName) {
        HomeDetailGuiHolder holder = new HomeDetailGuiHolder(player.getUniqueId(), slot);
        Inventory inventory = plugin.getServer().createInventory(holder, SIZE,
                ChatColor.DARK_AQUA + "Home: " + ChatColor.YELLOW + homeName);
        holder.setInventory(inventory);

        inventory.setItem(SLOT_TELEPORT, buildItem(Material.ENDER_PEARL, ChatColor.GREEN + "TP to home",
                List.of(ChatColor.GRAY + "Teleport to " + ChatColor.WHITE + homeName + ChatColor.GRAY + ".")));
        inventory.setItem(SLOT_DELETE, buildItem(Material.BARRIER, ChatColor.RED + "Delete home",
                List.of(ChatColor.GRAY + "Permanently deletes " + ChatColor.WHITE + homeName + ChatColor.GRAY + ".")));
        inventory.setItem(SLOT_BACK, buildItem(Material.ARROW, ChatColor.YELLOW + "Back",
                List.of(ChatColor.GRAY + "Back to your homes overview.")));

        player.openInventory(inventory);
    }

    private static ItemStack buildItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
