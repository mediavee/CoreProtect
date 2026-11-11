package net.coreprotect.utility;

import net.coreprotect.language.Phrase;
import net.coreprotect.language.Selector;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatUtils {

    public static final String PREFIX = "<gold>[Logs] <white>";
    public static final String LINE = "<gold><strikethrough>---------------------------</strikethrough>";
    public static final String ADD = "<green>✚";
    public static final String REMOVE = "<red>✖";
    public static final String NEUTRAL = "<gray>◆";

    private ChatUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String getCoordinates(String command, int worldId, int x, int y, int z, boolean displayWorld, boolean italic) {
        StringBuilder worldDisplay = new StringBuilder();
        if (displayWorld) {
            worldDisplay.append("/").append(WorldUtils.getWorldName(worldId));
        }

        DecimalFormat decimalFormat = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.ROOT));
        String teleportCommand = "/" + command + " teleport wid:" + worldId + " " + decimalFormat.format(x + 0.50) + " " + y + " " + decimalFormat.format(z + 0.50);
        String display = "<aqua>" + x + " " + y + " " + z + (worldDisplay.length() > 0 ? " <gray>" + worldDisplay.substring(1) : "");

        return "<click:run_command:'" + teleportCommand + "'><hover:show_text:'<gray>Clique pour te téléporter'>" + display + "</hover></click>";
    }

    public static String getPageNavigation(String command, int page, int totalPages) {
        String back = page > 1 ? "<gray><click:run_command:'/" + command + " l " + (page - 1) + "'>◀</click>" : "<dark_gray>◀";
        String next = page < totalPages ? "<gray><click:run_command:'/" + command + " l " + (page + 1) + "'>▶</click>" : "<dark_gray>▶";
        return back + " <gray>" + Phrase.build(Phrase.LOOKUP_PAGE, "<yellow>" + page + "<gray>/<yellow>" + totalPages) + " " + next;
    }

    public static String header(String title, String detail) {
        return LINE + "\n<yellow><bold>" + title + "</bold>" + (detail.isEmpty() ? "" : " <gray>◆ " + detail) + "\n<white> ";
    }

    public static String footer(String command, int page, int totalPages) {
        return "<white> \n" + (totalPages > 1 ? getPageNavigation(command, page, totalPages) + "\n" : "") + LINE;
    }

    public static String getTimeSince(long resultTime, long currentTime, boolean component) {
        StringBuilder message = new StringBuilder();
        double timeSince = currentTime - (resultTime + 0.00);
        if (timeSince < 0.00) {
            timeSince = 0.00;
        }

        DecimalFormat decimalFormat = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.FRANCE));

        // minutes
        timeSince = timeSince / 60;
        if (timeSince < 60.0) {
            message.append(Phrase.build(Phrase.LOOKUP_TIME, decimalFormat.format(timeSince) + " " + Phrase.build(Phrase.TIME_UNITS, Selector.FIRST)));
        }

        // hours
        if (message.length() == 0) {
            timeSince = timeSince / 60;
            if (timeSince < 24.0) {
                message.append(Phrase.build(Phrase.LOOKUP_TIME, decimalFormat.format(timeSince) + " " + Phrase.build(Phrase.TIME_UNITS, Selector.SECOND)));
            }
        }

        // days
        if (message.length() == 0) {
            timeSince = timeSince / 24;
            message.append(Phrase.build(Phrase.LOOKUP_TIME, decimalFormat.format(timeSince) + " " + Phrase.build(Phrase.TIME_UNITS, Selector.THIRD)));
        }

        if (component) {
            Date logDate = new Date(resultTime * 1000L);
            String formattedTimestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(logDate);

            return "<hover:show_text:'<gray>" + formattedTimestamp + "'><gray>" + message + "</hover>";
        }

        return message.toString();
    }

    /**
     * Convert a legacy section-formatted string (§-codes, as returned by Bukkit ItemMeta on 1.8)
     * into a MiniMessage string. Also escapes any MiniMessage tags present in the raw text,
     * preventing accidental tag injection from user-provided item names/lore.
     */
    public static String fromLegacy(String legacy) {
        if (legacy == null || legacy.isEmpty()) {
            return "";
        }

        return MiniMessage.miniMessage().serialize(LegacyComponentSerializer.legacySection().deserialize(legacy));
    }

    public static String createTooltip(String phrase, String tooltip) {
        if (tooltip.isEmpty()) {
            return phrase;
        }

        String escapedTooltip = tooltip.replace("'", "\\'");
        return "<hover:show_text:'" + escapedTooltip + "'>" + phrase + "</hover>";
    }

    public static void sendConsoleComponentStartup(String string) {
        Chat.console("[CoreProtect] " + string);
    }
} 