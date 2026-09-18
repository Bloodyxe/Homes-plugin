package de.dan.homes.util;

import java.util.regex.Pattern;

/**
 * Shared validation for home names, used both by /sethome and by the
 * chat-based naming flow triggered from the GUI.
 */
public final class HomeNames {

    public static final int MAX_LENGTH = 24;
    private static final Pattern VALID = Pattern.compile("[a-zA-Z0-9_-]+");

    private HomeNames() {
    }

    /**
     * Returns an error message if the name is invalid, or null if it's fine.
     */
    public static String validate(String name) {
        if (name == null || name.isEmpty()) {
            return "The home name cannot be empty.";
        }
        if (name.length() > MAX_LENGTH) {
            return "The name may only be up to " + MAX_LENGTH + " characters long.";
        }
        if (!VALID.matcher(name).matches()) {
            return "The name may only contain letters, digits, _ and -.";
        }
        return null;
    }
}
