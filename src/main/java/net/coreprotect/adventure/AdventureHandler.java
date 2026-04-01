package net.coreprotect.adventure;

import org.bukkit.plugin.Plugin;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class AdventureHandler {

    private static BukkitAudiences audiences;
    private static MiniMessage miniMessage;

    private AdventureHandler() {
        throw new IllegalStateException("Utility class");
    }

    public static void initialize(Plugin plugin) {
        audiences = BukkitAudiences.create(plugin);
        miniMessage = MiniMessage.miniMessage();
    }

    public static void shutdown() {
        if (audiences != null) {
            audiences.close();
            audiences = null;
        }
    }

    public static BukkitAudiences audiences() {
        return audiences;
    }

    public static MiniMessage miniMessage() {
        return miniMessage;
    }

}
