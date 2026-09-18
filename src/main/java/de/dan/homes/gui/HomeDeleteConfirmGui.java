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
 * "Are you sure?" screen shown after clicking "Delete home", so an
 * accidental click in the detail menu can't delete a home right away.
 */
public final class HomeDeleteConfirmGui {

    private static final int SIZE = 27;
    public static final int SLOT_CONFIRM = 11;
    public static final int SLOT_INFO = 13;
    public static final int SLOT_CANCEL = 15;

    private HomeDeleteConfirmGui() {
    }

    public static void open(HomesPlugin plugin, Player player, int slot, String homeName) {
        HomeDeleteConfirmGuiHolder holder = new HomeDeleteConfirmGuiHolder(player.getUniqueId(), slot, homeName);
        Inventory inventory = plugin.getServer().createInventory(holder, SIZE,
                ChatColor.RED + "Delete " + homeName + "?");
        holder.setInventory(inventory);

        inventory.setItem(SLOT_CONFIRM, buildItem(Material.RED_CONCRETE, ChatColor.RED + "Yes, delete it",
                List.of(ChatColor.GRAY + "Permanently deletes " + ChatColor.WHITE + homeName + ChatColor.GRAY + ".",
                        ChatColor.GRAY + "This cannot be undone.")));
        inventory.setItem(SLOT_INFO, buildItem(Material.BARRIER, ChatColor.YELLOW + "Delete " + homeName + "?",
                List.of(ChatColor.GRAY + "Are you sure you want to delete this home?")));
        inventory.setItem(SLOT_CANCEL, buildItem(Material.LIME_CONCRETE, ChatColor.GREEN + "No, keep it",
                List.of(ChatColor.GRAY + "Goes back without changing anything.")));

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
