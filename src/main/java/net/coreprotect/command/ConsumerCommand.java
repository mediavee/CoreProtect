package net.coreprotect.command;

import java.util.Locale;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import net.coreprotect.config.ConfigHandler;
import net.coreprotect.language.Phrase;
import net.coreprotect.language.Selector;
import net.coreprotect.utility.Chat;

public class ConsumerCommand {

    private ConsumerCommand() {
        throw new IllegalStateException("Command class");
    }

    protected static void runCommand(final CommandSender player, boolean permission, String[] args) {
        if (!permission) {
            Chat.send(player, Phrase.build(Phrase.NO_PERMISSION));
            return;
        }
        if (!(player instanceof ConsoleCommandSender)) {
            Chat.send(player, Phrase.build(Phrase.COMMAND_CONSOLE));
            return;
        }
        if (ConfigHandler.converterRunning) {
            Chat.send(player, Phrase.build(Phrase.UPGRADE_IN_PROGRESS));
            return;
        }
        if (ConfigHandler.purgeRunning) {
            Chat.send(player, Phrase.build(Phrase.PURGE_IN_PROGRESS));
            return;
        }

        if (args.length == 2) {
            String action = args[1].toLowerCase(Locale.ROOT);
            boolean pauseCommand = (action.equals("pause") || action.equals("disable") || action.equals("stop"));
            boolean resumeCommand = (action.equals("resume") || action.equals("enable") || action.equals("start"));

            if (pauseCommand || resumeCommand) {
                if (ConfigHandler.pauseConsumer) {
                    if (pauseCommand) {
                        Chat.send(player, Phrase.build(Phrase.CONSUMER_ERROR, Selector.FIRST)); // already paused
                    }
                    else {
                        ConfigHandler.pauseConsumer = false;
                        Chat.send(player, Phrase.build(Phrase.CONSUMER_TOGGLED, Selector.SECOND)); // now started
                    }
                }
                else {
                    if (resumeCommand) {
                        Chat.send(player, Phrase.build(Phrase.CONSUMER_ERROR, Selector.SECOND)); // already running
                    }
                    else {
                        ConfigHandler.pauseConsumer = true;
                        Chat.send(player, Phrase.build(Phrase.CONSUMER_TOGGLED, Selector.FIRST)); // now paused
                    }
                }
                return;
            }
        }

        Chat.send(player, "<dark_aqua>CoreProtect <white>- " + Phrase.build(Phrase.MISSING_PARAMETERS, "<white>", "/co consumer \\<pause|resume>"));
    }

}
