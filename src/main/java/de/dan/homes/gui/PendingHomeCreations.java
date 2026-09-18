package de.dan.homes.gui;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remembers, for a player who just clicked an empty home slot, which slot
 * they clicked - so that their next chat message can be used as the name
 * for that slot instead of being sent to public chat.
 */
public class PendingHomeCreations {

    private final Map<UUID, Integer> pendingSlotByPlayer = new ConcurrentHashMap<>();

    public void start(UUID uuid, int slot) {
        pendingSlotByPlayer.put(uuid, slot);
    }

    public boolean isPending(UUID uuid) {
        return pendingSlotByPlayer.containsKey(uuid);
    }

    /**
     * Removes and returns the pending slot for this player, or null if none.
     */
    public Integer consume(UUID uuid) {
        return pendingSlotByPlayer.remove(uuid);
    }

    public void cancel(UUID uuid) {
        pendingSlotByPlayer.remove(uuid);
    }
}
