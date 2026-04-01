package net.coreprotect.command;

import java.util.Locale;

import org.bukkit.command.CommandSender;

import net.coreprotect.language.Phrase;
import net.coreprotect.language.Selector;
import net.coreprotect.utility.Chat;

public class HelpCommand {
    protected static void runCommand(CommandSender player, boolean permission, String[] args) {
        int resultc = args.length;
        if (permission) {
            if (resultc > 1) {
                String helpcommand_original = args[1];
                String helpcommand = args[1].toLowerCase(Locale.ROOT);
                helpcommand = helpcommand.replaceAll("[^a-zA-Z]", "");
                Chat.send(player, "<white>----- <dark_aqua>" + Phrase.build(Phrase.HELP_HEADER, "CoreProtect") + " <white>-----");
                if (helpcommand.equals("help")) {
                    Chat.send(player, "<dark_aqua>/co help <white>- " + Phrase.build(Phrase.HELP_LIST));
                }
                else if (helpcommand.equals("inspect") || helpcommand.equals("inspector") || helpcommand.equals("in")) {
                    Chat.send(player, "<dark_aqua>" + Phrase.build(Phrase.HELP_INSPECT_1));
                    Chat.send(player, "* " + Phrase.build(Phrase.HELP_INSPECT_2));
                    Chat.send(player, "* " + Phrase.build(Phrase.HELP_INSPECT_3));
                    Chat.send(player, "* " + Phrase.build(Phrase.HELP_INSPECT_4));
                    Chat.send(player, "* " + Phrase.build(Phrase.HELP_INSPECT_5));
                    Chat.send(player, "* " + Phrase.build(Phrase.HELP_INSPECT_6));
                    Chat.send(player, "<gray><italic>" + Phrase.build(Phrase.HELP_INSPECT_7));
                }
                else if (helpcommand.equals("params") || helpcommand.equals("param") || helpcommand.equals("parameters") || helpcommand.equals("parameter")) {
                    Chat.send(player, "<dark_aqua>/co lookup <gray>\\<params> <white>- " + Phrase.build(Phrase.HELP_PARAMS_1, Selector.FIRST));
                    Chat.send(player, "<dark_aqua>| <gray>u:\\<users> <white>- " + Phrase.build(Phrase.HELP_PARAMS_2, Selector.FIRST));
                    Chat.send(player, "<dark_aqua>| <gray>t:\\<time> <white>- " + Phrase.build(Phrase.HELP_PARAMS_3, Selector.FIRST));
                    Chat.send(player, "<dark_aqua>| <gray>r:\\<radius> <white>- " + Phrase.build(Phrase.HELP_PARAMS_4, Selector.FIRST));
                    Chat.send(player, "<dark_aqua>| <gray>a:\\<action> <white>- " + Phrase.build(Phrase.HELP_PARAMS_5, Selector.FIRST));
                    Chat.send(player, "<dark_aqua>| <gray>i:\\<include> <white>- " + Phrase.build(Phrase.HELP_PARAMS_6, Selector.FIRST));
                    Chat.send(player, "<dark_aqua>| <gray>e:\\<exclude> <white>- " + Phrase.build(Phrase.HELP_PARAMS_7, Selector.FIRST));
                    Chat.send(player, "<gray><italic>" + Phrase.build(Phrase.HELP_PARAMETER, "/co help \\<param>"));
                }
                else if (helpcommand.equals("rollback") || helpcommand.equals("rollbacks") || helpcommand.equals("rb") || helpcommand.equals("ro")) {
                    Chat.send(player, "<dark_aqua>/co rollback <gray>\\<params> <white>- " + Phrase.build(Phrase.HELP_PARAMS_1, Selector.SECOND));
                    Chat.send(player, "<dark_aqua>| <gray>u:\\<users> <white>- " + Phrase.build(Phrase.HELP_PARAMS_2, Selector.SECOND));
                    Chat.send(player, "<dark_aqua>| <gray>t:\\<time> <white>- " + Phrase.build(Phrase.HELP_PARAMS_3, Selector.SECOND));
                    Chat.send(player, "<dark_aqua>| <gray>r:\\<radius> <white>- " + Phrase.build(Phrase.HELP_PARAMS_4, Selector.SECOND));
                    Chat.send(player, "<dark_aqua>| <gray>a:\\<action> <white>- " + Phrase.build(Phrase.HELP_PARAMS_5, Selector.SECOND));
                    Chat.send(player, "<dark_aqua>| <gray>i:\\<include> <white>- " + Phrase.build(Phrase.HELP_PARAMS_6, Selector.SECOND));
                    Chat.send(player, "<dark_aqua>| <gray>e:\\<exclude> <white>- " + Phrase.build(Phrase.HELP_PARAMS_7, Selector.SECOND));
                    Chat.send(player, "<gray><italic>" + Phrase.build(Phrase.HELP_PARAMETER, "/co help \\<param>"));
                }
                else if (helpcommand.equals("restore") || helpcommand.equals("restores") || helpcommand.equals("re") || helpcommand.equals("rs")) {
                    Chat.send(player, "<dark_aqua>/co restore <gray>\\<params> <white>- " + Phrase.build(Phrase.HELP_PARAMS_1, Selector.THIRD));
                    Chat.send(player, "<dark_aqua>| <gray>u:\\<users> <white>- " + Phrase.build(Phrase.HELP_PARAMS_2, Selector.THIRD));
                    Chat.send(player, "<dark_aqua>| <gray>t:\\<time> <white>- " + Phrase.build(Phrase.HELP_PARAMS_3, Selector.THIRD));
                    Chat.send(player, "<dark_aqua>| <gray>r:\\<radius> <white>- " + Phrase.build(Phrase.HELP_PARAMS_4, Selector.THIRD));
                    Chat.send(player, "<dark_aqua>| <gray>a:\\<action> <white>- " + Phrase.build(Phrase.HELP_PARAMS_5, Selector.THIRD));
                    Chat.send(player, "<dark_aqua>| <gray>i:\\<include> <white>- " + Phrase.build(Phrase.HELP_PARAMS_6, Selector.THIRD));
                    Chat.send(player, "<dark_aqua>| <gray>e:\\<exclude> <white>- " + Phrase.build(Phrase.HELP_PARAMS_7, Selector.THIRD));
                    Chat.send(player, "<gray><italic>" + Phrase.build(Phrase.HELP_PARAMETER, "/co help \\<param>"));
                }
                else if (helpcommand.equals("lookup") || helpcommand.equals("lookups") || helpcommand.equals("l")) {
                    Chat.send(player, "<dark_aqua>/co lookup \\<params>");
                    Chat.send(player, "<dark_aqua>/co l \\<params> <white>- " + Phrase.build(Phrase.HELP_LOOKUP_1));
                    Chat.send(player, "<dark_aqua>/co lookup \\<page> <white>- " + Phrase.build(Phrase.HELP_LOOKUP_2));
                    Chat.send(player, "<gray><italic>" + Phrase.build(Phrase.HELP_PARAMETER, "/co help params"));
                }
                else if (helpcommand.equals("purge") || helpcommand.equals("purges")) {
                    Chat.send(player, "<dark_aqua>/co purge t:\\<time> <white>- " + Phrase.build(Phrase.HELP_PURGE_1));
                    Chat.send(player, "<gray><italic>" + Phrase.build(Phrase.HELP_PURGE_2, "/co purge t:30d"));
                }
                else if (helpcommand.equals("reload")) {
                    Chat.send(player, "<dark_aqua>/co reload <white>- " + Phrase.build(Phrase.HELP_RELOAD_COMMAND));
                }
                else if (helpcommand.equals("status")) {
                    Chat.send(player, "<dark_aqua>/co status <white>- " + Phrase.build(Phrase.HELP_STATUS));
                }
                else if (helpcommand.equals("teleport")) {
                    Chat.send(player, "<dark_aqua>/co teleport \\<world> \\<x> \\<y> \\<z> <white>- " + Phrase.build(Phrase.HELP_TELEPORT));
                }
                else if (helpcommand.equals("u") || helpcommand.equals("user") || helpcommand.equals("users") || helpcommand.equals("uuser") || helpcommand.equals("uusers")) {
                    Chat.send(player, "<dark_aqua>/co lookup u:\\<users> <white>- " + Phrase.build(Phrase.HELP_USER_1));
                    Chat.send(player, "<gray><italic>" + Phrase.build(Phrase.HELP_USER_2));
                }
                else if (helpcommand.equals("t") || helpcommand.equals("time") || helpcommand.equals("ttime")) {
                    Chat.send(player, "<dark_aqua>/co lookup t:\\<time> <white>- " + Phrase.build(Phrase.HELP_TIME_1));
                    Chat.send(player, "<gray><italic>" + Phrase.build(Phrase.HELP_TIME_2));
                }
                else if (helpcommand.equals("r") || helpcommand.equals("radius") || helpcommand.equals("rradius")) {
                    Chat.send(player, "<dark_aqua>/co lookup r:\\<radius> <white>- " + Phrase.build(Phrase.HELP_RADIUS_1));
                    Chat.send(player, "<gray><italic>" + Phrase.build(Phrase.HELP_RADIUS_2));
                }
                else if (helpcommand.equals("a") || helpcommand.equals("action") || helpcommand.equals("actions") || helpcommand.equals("aaction")) {
                    Chat.send(player, "<dark_aqua>/co lookup a:\\<action> <white>- " + Phrase.build(Phrase.HELP_ACTION_1));
                    Chat.send(player, "<gray><italic>" + Phrase.build(Phrase.HELP_ACTION_2));
                }
                else if (helpcommand.equals("i") || helpcommand.equals("include") || helpcommand.equals("iinclude") || helpcommand.equals("b") || helpcommand.equals("block") || helpcommand.equals("blocks") || helpcommand.equals("bblock") || helpcommand.equals("bblocks")) {
                    Chat.send(player, "<dark_aqua>/co lookup i:\\<include> <white>- " + Phrase.build(Phrase.HELP_INCLUDE_1));
                    Chat.send(player, "<gray><italic>" + Phrase.build(Phrase.HELP_INCLUDE_2));
                    Chat.send(player, "<gray><italic>" + Phrase.build(Phrase.LINK_WIKI_BLOCK, "https://coreprotect.net/wiki-blocks"));
                    Chat.send(player, "<gray><italic>" + Phrase.build(Phrase.LINK_WIKI_ENTITY, "https://coreprotect.net/wiki-entities"));
                }
                else if (helpcommand.equals("e") || helpcommand.equals("exclude") || helpcommand.equals("eexclude")) {
                    Chat.send(player, "<dark_aqua>/co lookup e:\\<exclude> <white>- " + Phrase.build(Phrase.HELP_EXCLUDE_1));
                    Chat.send(player, "<gray><italic>" + Phrase.build(Phrase.HELP_EXCLUDE_2));
                    Chat.send(player, "<gray><italic>" + Phrase.build(Phrase.LINK_WIKI_BLOCK, "https://coreprotect.net/wiki-blocks"));
                }
                else {
                    Chat.send(player, "<white>" + Phrase.build(Phrase.HELP_NO_INFO, "<white>", "/co help " + helpcommand_original));
                }
            }
            else {
                Chat.send(player, "<white>----- <dark_aqua>" + Phrase.build(Phrase.HELP_HEADER, "CoreProtect") + " <white>-----");
                Chat.send(player, "<dark_aqua>/co help <gray>\\<command> <white>- " + Phrase.build(Phrase.HELP_COMMAND));
                Chat.send(player, "<dark_aqua>/co <gray>inspect <white>- " + Phrase.build(Phrase.HELP_INSPECT_COMMAND));
                Chat.send(player, "<dark_aqua>/co <gray>rollback <dark_aqua>\\<params> <white>- " + Phrase.build(Phrase.HELP_ROLLBACK_COMMAND));
                Chat.send(player, "<dark_aqua>/co <gray>restore <dark_aqua>\\<params> <white>- " + Phrase.build(Phrase.HELP_RESTORE_COMMAND));
                Chat.send(player, "<dark_aqua>/co <gray>lookup <dark_aqua>\\<params> <white>- " + Phrase.build(Phrase.HELP_LOOKUP_COMMAND));
                Chat.send(player, "<dark_aqua>/co <gray>purge <dark_aqua>\\<params> <white>- " + Phrase.build(Phrase.HELP_PURGE_COMMAND));
                Chat.send(player, "<dark_aqua>/co <gray>reload <white>- " + Phrase.build(Phrase.HELP_RELOAD_COMMAND));
                Chat.send(player, "<dark_aqua>/co <gray>status <white>- " + Phrase.build(Phrase.HELP_STATUS_COMMAND));
            }
        }
        else {
            Chat.send(player, Phrase.build(Phrase.NO_PERMISSION));
        }
    }
}
