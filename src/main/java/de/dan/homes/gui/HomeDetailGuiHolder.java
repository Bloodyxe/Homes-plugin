package de.dan.homes.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

/**
 * Marks an inventory as the detail view for a single home slot ("TP to
 * home" / "Delete home"). Remembers which slot it is about, so the click
 * handler knows which home to act on.
 */
public class HomeDetailGuiHolder implements InventoryHolder {

    private final UUID owner;
    private final int slot;
    private Inventory inventory;

    public HomeDetailGuiHolder(UUID owner, int slot) {
        this.owner = owner;
        this.slot = slot;
    }

    public UUID getOwner() {
        return owner;
    }

    public int getSlot() {
        return slot;
    }

    void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
