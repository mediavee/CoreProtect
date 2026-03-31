package net.coreprotect.spigot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.coreprotect.config.Config;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.Color;
import net.coreprotect.utility.StringUtils;
import net.coreprotect.utility.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class SpigotHandler extends SpigotAdapter implements SpigotInterface {

    public SpigotHandler() {
        Color.DARK_AQUA = ChatColor.DARK_AQUA.toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addHoverComponent(Object message, String[] data) {
        try {
            if (Config.getGlobal().HOVER_EVENTS) {
                String tooltipText = data[1];
                BaseComponent[] displayComponent = TextComponent.fromLegacyText(processComponent(tooltipText));
                BaseComponent[] textParts = TextComponent.fromLegacyText(data[2]);
                for (BaseComponent part : textParts) {
                    part.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, displayComponent));
                }
                Collections.addAll((List<BaseComponent>) message, textParts);
            }
            else {
                BaseComponent[] textParts = TextComponent.fromLegacyText(data[2]);
                Collections.addAll((List<BaseComponent>) message, textParts);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setHoverEvent(Object component, String text) {
        if (Config.getGlobal().HOVER_EVENTS) {
            ((TextComponent) component).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(text)));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sendComponent(CommandSender sender, String string, String bypass) {
        List<BaseComponent> parts = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        Matcher matcher = Util.tagParser.matcher(string);
        while (matcher.find()) {
            String value = matcher.group(1);
            if (value != null) {
                if (builder.length() > 0) {
                    flushBuilder(parts, builder);
                }

                String[] data = value.split("\\|", 3);
                if (data[0].equals(Chat.COMPONENT_COMMAND)) {
                    TextComponent component = new TextComponent(TextComponent.fromLegacyText(data[2]));
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, data[1]));
                    SpigotAdapter.ADAPTER.setHoverEvent(component, StringUtils.hoverCommandFilter(data[1]));
                    parts.add(component);
                }
                else if (data[0].equals(Chat.COMPONENT_POPUP)) {
                    SpigotAdapter.ADAPTER.addHoverComponent(parts, data);
                }
            }
            else {
                builder.append(matcher.group(2));
            }
        }

        if (builder.length() > 0) {
            flushBuilder(parts, builder);
        }

        if (bypass != null) {
            TextComponent bypassComponent = new TextComponent(bypass);
            bypassComponent.setColor(ChatColor.WHITE);
            parts.add(bypassComponent);
        }

        BaseComponent[] result = parts.toArray(new BaseComponent[0]);
        if (sender instanceof Player) {
            ((Player) sender).spigot().sendMessage(result);
        }
        else {
            sender.sendMessage(TextComponent.toLegacyText(result));
        }
    }

    private static void flushBuilder(List<BaseComponent> parts, StringBuilder builder) {
        Collections.addAll(parts, TextComponent.fromLegacyText(builder.toString()));
        builder.setLength(0);
    }
}
