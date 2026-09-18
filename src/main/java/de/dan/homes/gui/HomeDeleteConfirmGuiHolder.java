package de.dan.homes.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

/**
 * Marks an inventory as the "are you sure?" screen shown before a home is
 * actually deleted.
 */
public class HomeDeleteConfirmGuiHolder implements InventoryHolder {

    private final UUID owner;
    private final int slot;
    private final String homeName;
    private Inventory inventory;

    public HomeDeleteConfirmGuiHolder(UUID owner, int slot, String homeName) {
        this.owner = owner;
        this.slot = slot;
        this.homeName = homeName;
    }

    public UUID getOwner() {
        return owner;
    }

    public int getSlot() {
        return slot;
    }

    public String getHomeName() {
        return homeName;
    }

    void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
