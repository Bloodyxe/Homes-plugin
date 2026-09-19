package de.dan.homes.gui;

import de.dan.homes.HomesPlugin;
import de.dan.homes.config.HomeLimitService;
import de.dan.homes.storage.Home;
import de.dan.homes.storage.HomeManager;
import de.dan.homes.teleport.HomeTeleportChannel;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class HomesGuiListener implements Listener {

    private final HomesPlugin plugin;
    private final HomeManager homeManager;
    private final HomeLimitService limitService;
    private final PendingHomeCreations pendingHomeCreations;
    private final HomeTeleportChannel teleportChannel;

    public HomesGuiListener(HomesPlugin plugin, HomeManager homeManager, HomeLimitService limitService,
                             PendingHomeCreations pendingHomeCreations, HomeTeleportChannel teleportChannel) {
        this.plugin = plugin;
        this.homeManager = homeManager;
        this.limitService = limitService;
        this.pendingHomeCreations = pendingHomeCreations;
        this.teleportChannel = teleportChannel;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof HomesGuiHolder holder) {
            handleMainClick(event, holder);
        } else if (event.getInventory().getHolder() instanceof HomeDetailGuiHolder holder) {
            handleDetailClick(event, holder);
        } else if (event.getInventory().getHolder() instanceof HomeDeleteConfirmGuiHolder holder) {
            handleDeleteConfirmClick(event, holder);
        }
    }

    private void handleMainClick(InventoryClickEvent event, HomesGuiHolder holder) {
        // Cancelled unconditionally: nobody may move items in or out of this
        // GUI, and every action below is still hard-checked against the
        // real clicker so a shared/duplicated view can't be used to act for
        // someone else.
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player clicker) || !clicker.getUniqueId().equals(holder.getOwner())) {
            return;
        }

        int maxHomes = limitService.getMaxHomes(clicker);
        int homeIndex = HomesGui.inventorySlotToHomeIndex(event.getRawSlot());
        if (homeIndex == -1 || homeIndex >= maxHomes) {
            return; // border/filler/locked slot, or a click in the player's own inventory
        }

        Home home = homeManager.getHomeAtSlot(clicker.getUniqueId(), homeIndex);
        if (home != null) {
            // Switch directly to the detail GUI without closing first, so
            // the client doesn't flash back to the game world in between -
            // that flash is what resets the mouse cursor to the middle of
            // the screen.
            HomeDetailGui.open(plugin, clicker, homeIndex, home.getName());
            return;
        }

        clicker.closeInventory();
        pendingHomeCreations.start(clicker.getUniqueId(), homeIndex);
        clicker.sendMessage(ChatColor.YELLOW + "Type the name for this home in chat (or type "
                + ChatColor.RED + "cancel" + ChatColor.YELLOW + " to abort).");
    }

    private void handleDetailClick(InventoryClickEvent event, HomeDetailGuiHolder holder) {
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player clicker) || !clicker.getUniqueId().equals(holder.getOwner())) {
            return;
        }

        int slot = event.getRawSlot();
        int homeSlot = holder.getSlot();

        if (slot == HomeDetailGui.SLOT_TELEPORT) {
            Home home = homeManager.getHomeAtSlot(clicker.getUniqueId(), homeSlot);
            if (home == null) {
                clicker.sendMessage(ChatColor.RED + "This home no longer exists.");
                clicker.closeInventory();
                return;
            }
            clicker.closeInventory();
            if (!teleportChannel.start(plugin, clicker, home.getName(), home.getLocation())) {
                clicker.sendMessage(ChatColor.RED + "You are already teleporting. Please wait.");
            }
            return;
        }

        if (slot == HomeDetailGui.SLOT_DELETE) {
            Home home = homeManager.getHomeAtSlot(clicker.getUniqueId(), homeSlot);
            if (home == null) {
                clicker.sendMessage(ChatColor.RED + "This home no longer exists.");
                clicker.closeInventory();
                return;
            }
            // Same as above: switch straight to the confirm GUI, no close in
            // between, so the cursor doesn't jump.
            HomeDeleteConfirmGui.open(plugin, clicker, homeSlot, home.getName());
            return;
        }

        if (slot == HomeDetailGui.SLOT_BACK) {
            HomesGui.open(plugin, homeManager, limitService, clicker);
        }
    }

    private void handleDeleteConfirmClick(InventoryClickEvent event, HomeDeleteConfirmGuiHolder holder) {
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player clicker) || !clicker.getUniqueId().equals(holder.getOwner())) {
            return;
        }

        int slot = event.getRawSlot();
        int homeSlot = holder.getSlot();

        if (slot == HomeDeleteConfirmGui.SLOT_CONFIRM) {
            boolean removed = homeManager.deleteHomeAtSlot(clicker.getUniqueId(), homeSlot);
            clicker.sendMessage(removed
                    ? ChatColor.GREEN + "Home " + ChatColor.YELLOW + holder.getHomeName()
                        + ChatColor.GREEN + " has been deleted."
                    : ChatColor.RED + "This home no longer exists.");
            HomesGui.open(plugin, homeManager, limitService, clicker);
            return;
        }

        if (slot == HomeDeleteConfirmGui.SLOT_CANCEL) {
            Home home = homeManager.getHomeAtSlot(clicker.getUniqueId(), homeSlot);
            if (home == null) {
                clicker.sendMessage(ChatColor.RED + "This home no longer exists.");
                HomesGui.open(plugin, homeManager, limitService, clicker);
                return;
            }
            HomeDetailGui.open(plugin, clicker, homeSlot, home.getName());
        }
    }
}
