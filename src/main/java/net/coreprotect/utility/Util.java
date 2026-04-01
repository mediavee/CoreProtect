package net.coreprotect.utility;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import net.coreprotect.consumer.Queue;

/**
 * Central utility class that provides access to various utility functions.
 * Most methods delegate to specialized utility classes.
 */
public class Util extends Queue {

    private Util() {
        throw new IllegalStateException("Utility class");
    }

    @SuppressWarnings("deprecation")
    public static void sendBlockChange(Player player, Location location, Material material, byte data) {
        player.sendBlockChange(location, material, data);
    }
}
