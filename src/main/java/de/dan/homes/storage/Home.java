package de.dan.homes.storage;

import org.bukkit.Location;

/**
 * A single home of a player. Immutable - to rename/move it, a new Home
 * object is simply created.
 */
public final class Home {

    private final String name;
    private final Location location;

    public Home(String name, Location location) {
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }
}
