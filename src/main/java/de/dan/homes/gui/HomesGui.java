package de.dan.homes.gui;

import de.dan.homes.HomesPlugin;
import de.dan.homes.storage.Home;
import de.dan.homes.storage.HomeManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds and opens the main Homes overview GUI for a player: one fixed box
 * per home slot the player is allowed to have (their current max-homes
 * limit, see HomeLimitService). A box shows the home's name if that slot is
 * occupied, or a "click to sethome" placeholder if it's still free.
 *
 * Only the homes of the given player are ever shown - a player can only
 * open this GUI for themself (see HomesCommand).
 */
public final class HomesGui {

    private HomesGui() {
    }

    public static void open(HomesPlugin plugin, HomeManager homeManager, Player player, int maxHomes) {
        int size = inventorySize(maxHomes);
        Map<Integer, Home> homes = homeManager.getHomesBySlot(player.getUniqueId());

        HomesGuiHolder holder = new HomesGuiHolder(player.getUniqueId());
        Inventory inventory = plugin.getServer().createInventory(holder, size, ChatColor.DARK_AQUA + "Your Homes");
        holder.setInventory(inventory);

        for (int slot = 0; slot < size; slot++) {
            if (slot >= maxHomes) {
                inventory.setItem(slot, buildFillerItem());
                continue;
            }
            Home home = homes.get(slot);
            inventory.setItem(slot, home != null ? buildOccupiedItem(player, home) : buildEmptyItem());
        }

        player.openInventory(inventory);
    }

    private static int inventorySize(int maxHomes) {
        int size = ((Math.max(1, maxHomes) + 8) / 9) * 9;
        return Math.max(9, Math.min(54, size));
    }

    private static ItemStack buildOccupiedItem(Player owner, Home home) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(owner);
        meta.setDisplayName(ChatColor.GREEN + home.getName());

        Location loc = home.getLocation();
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "World: " + ChatColor.WHITE + loc.getWorld().getName());
        lore.add(ChatColor.GRAY + "Position: " + ChatColor.WHITE
                + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click to manage this home");
        meta.setLore(lore);

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
