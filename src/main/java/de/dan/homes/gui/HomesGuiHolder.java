package de.dan.homes.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

/**
 * Marks an inventory as the main Homes overview GUI. Each slot in this
 * inventory directly corresponds to a home "slot" number (0, 1, 2, ...) -
 * there is no separate mapping, the raw inventory slot IS the home slot.
 */
public class HomesGuiHolder implements InventoryHolder {

    private final UUID owner;
    private Inventory inventory;

    public HomesGuiHolder(UUID owner) {
        this.owner = owner;
    }

    public UUID getOwner() {
        return owner;
    }

    void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
