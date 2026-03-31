package net.coreprotect.spigot;

import java.util.regex.Matcher;

import org.bukkit.command.CommandSender;

import net.coreprotect.config.ConfigHandler;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.Util;

public class SpigotAdapter implements SpigotInterface {

    public static SpigotInterface ADAPTER;

    public static void loadAdapter() {
        if (ConfigHandler.isSpigot) {
            SpigotAdapter.ADAPTER = new SpigotHandler();
        }
        else {
            SpigotAdapter.ADAPTER = new SpigotAdapter();
        }
    }

    @Override
    public void addHoverComponent(Object message, String[] data) {
    }

    @Override
    public void setHoverEvent(Object message, String text) {
    }

    @Override
    public void sendComponent(CommandSender sender, String string, String bypass) {
        StringBuilder message = new StringBuilder();

        Matcher matcher = Util.tagParser.matcher(string);
        while (matcher.find()) {
            String value = matcher.group(1);
            if (value != null) {
                String[] data = value.split("\\|", 3);
                if (data[0].equals(Chat.COMPONENT_COMMAND) || data[0].equals(Chat.COMPONENT_POPUP)) {
                    message.append(data[2]);
                }
            }
            else {
                message.append(matcher.group(2));
            }
        }

        if (bypass != null) {
            message.append(bypass);
        }

        Chat.sendMessage(sender, message.toString());
    }

    public String processComponent(String component) {
        return component.replace(Chat.COMPONENT_PIPE, "|");
    }
}
