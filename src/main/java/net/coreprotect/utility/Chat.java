package net.coreprotect.utility;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import net.coreprotect.adventure.AdventureHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public final class Chat {

    private Chat() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Send a MiniMessage-formatted string to a CommandSender via Adventure.
     */
    public static void send(CommandSender sender, String miniMessage) {
        if (miniMessage == null || miniMessage.isEmpty()) {
            return;
        }
        Component component = AdventureHandler.miniMessage().deserialize(miniMessage);
        AdventureHandler.audiences().sender(sender).sendMessage(component);
    }

    /**
     * Send an Adventure Component to a CommandSender.
     */
    public static void send(CommandSender sender, Component component) {
        AdventureHandler.audiences().sender(sender).sendMessage(component);
    }

    /**
     * Log a MiniMessage string to console (strips all tags).
     */
    public static void console(String string) {
        String plain = string;
        if (AdventureHandler.miniMessage() != null) {
            try {
                Component component = AdventureHandler.miniMessage().deserialize(string);
                plain = PlainTextComponentSerializer.plainText().serialize(component);
            }
            catch (Exception e) {
                // Fallback to raw string if MiniMessage parsing fails
            }
        }

        if (plain.startsWith("-") || plain.startsWith("[")) {
            Bukkit.getLogger().log(Level.INFO, plain);
        }
        else {
            Bukkit.getLogger().log(Level.INFO, "[CoreProtect] " + plain);
        }
    }

    /**
     * Send a MiniMessage string to console + all online ops + the sender.
     */
    public static void sendGlobalMessage(CommandSender user, String miniMessage) {
        Component component = AdventureHandler.miniMessage().deserialize(miniMessage);
        String plain = PlainTextComponentSerializer.plainText().serialize(component);

        if (user instanceof ConsoleCommandSender) {
            AdventureHandler.audiences().sender(user).sendMessage(component);
            return;
        }

        Bukkit.getServer().getConsoleSender().sendMessage("[CoreProtect] " + plain);
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (player.isOp() && !player.getName().equals(user.getName())) {
                AdventureHandler.audiences().player(player).sendMessage(component);
            }
        }
        if (user instanceof Player && ((Player) user).isOnline()) {
            AdventureHandler.audiences().player((Player) user).sendMessage(component);
        }
    }

}
