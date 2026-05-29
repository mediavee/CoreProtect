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
        String display = "<gray>" + (italic ? "<italic>" : "") + "(x" + x + "/y" + y + "/z" + z + worldDisplay + ")";

        return "<click:run_command:'" + teleportCommand + "'>" + display + "</click>";
    }

    public static String getPageNavigation(String command, int page, int totalPages) {
        StringBuilder message = new StringBuilder();

        // back arrow
        String backArrow = "";
        if (page > 1) {
            backArrow = "<click:run_command:'/" + command + " l " + (page - 1) + "'>◀ </click>";
        }

        // next arrow
        String nextArrow = " ";
        if (page < totalPages) {
            nextArrow = "<click:run_command:'/" + command + " l " + (page + 1) + "'> ▶ </click>";
        }

        StringBuilder pagination = new StringBuilder();
        if (totalPages > 1) {
            pagination.append("<gray>(");
            if (page > 3) {
                pagination.append("<white><click:run_command:'/" + command + " l 1'>1 </click>");
                if (page > 4 && totalPages > 7) {
                    pagination.append("<gray>... ");
                }
                else {
                    pagination.append("<gray>| ");
                }
            }

            int displayStart = (page - 2) < 1 ? 1 : (page - 2);
            int displayEnd = (page + 2) > totalPages ? totalPages : (page + 2);
            if (page > 999 || (page > 101 && totalPages > 99999)) { // limit to max 5 page numbers
                displayStart = (displayStart + 1) < displayEnd ? (displayStart + 1) : displayStart;
                displayEnd = (displayEnd - 1) > displayStart ? (displayEnd - 1) : displayEnd;
                if (displayStart > (totalPages - 3)) {
                    displayStart = (totalPages - 3) < 1 ? 1 : (totalPages - 3);
                }
            }
            else { // display at least 7 page numbers
                if (displayStart > (totalPages - 5)) {
                    displayStart = (totalPages - 5) < 1 ? 1 : (totalPages - 5);
                }
                if (displayEnd < 6) {
                    displayEnd = 6 > totalPages ? totalPages : 6;
                }
            }

            if (page > 99999) { // limit to max 3 page numbers
                displayStart = (displayStart + 1) < displayEnd ? (displayStart + 1) : displayStart;
                displayEnd = (displayEnd - 1) >= displayStart ? (displayEnd - 1) : displayEnd;
                if (page == (totalPages - 1)) {
                    displayEnd = totalPages - 1;
                }
                if (displayStart < displayEnd) {
                    displayStart = displayEnd;
                }
            }

            if (page > 3 && displayStart == 1) {
                displayStart = 2;
            }

            for (int displayPage = displayStart; displayPage <= displayEnd; displayPage++) {
                if (page != displayPage) {
                    pagination.append("<white><click:run_command:'/" + command + " l " + displayPage + "'>" + displayPage + (displayPage < totalPages ? " " : "") + "</click>");
                }
                else {
                    pagination.append("<white><underlined>" + displayPage + "</underlined>" + (displayPage < totalPages ? " " : ""));
                }
                if (displayPage < displayEnd) {
                    pagination.append("<gray>| ");
                }
            }

            if (displayEnd < totalPages) {
                if (displayEnd < (totalPages - 1)) {
                    pagination.append("<gray>... ");
                }
                else {
                    pagination.append("<gray>| ");
                }
                if (page != totalPages) {
                    pagination.append("<white><click:run_command:'/" + command + " l " + totalPages + "'>" + totalPages + "</click>");
                }
                else {
                    pagination.append("<white><underlined>" + totalPages + "</underlined>");
                }
            }

            pagination.append("<gray>)");
        }

        return message.append("<white>" + backArrow + "<dark_aqua>" + Phrase.build(Phrase.LOOKUP_PAGE, "<white>" + page + "/" + totalPages) + nextArrow + pagination).toString();
    }

    public static String getTimeSince(long resultTime, long currentTime, boolean component) {
        StringBuilder message = new StringBuilder();
        double timeSince = currentTime - (resultTime + 0.00);
        if (timeSince < 0.00) {
            timeSince = 0.00;
        }

        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        // minutes
        timeSince = timeSince / 60;
        if (timeSince < 60.0) {
            message.append(Phrase.build(Phrase.LOOKUP_TIME, decimalFormat.format(timeSince) + Phrase.build(Phrase.TIME_UNITS, Selector.FIRST)));
        }

        // hours
        if (message.length() == 0) {
            timeSince = timeSince / 60;
            if (timeSince < 24.0) {
                message.append(Phrase.build(Phrase.LOOKUP_TIME, decimalFormat.format(timeSince) + Phrase.build(Phrase.TIME_UNITS, Selector.SECOND)));
            }
        }

        // days
        if (message.length() == 0) {
            timeSince = timeSince / 24;
            message.append(Phrase.build(Phrase.LOOKUP_TIME, decimalFormat.format(timeSince) + Phrase.build(Phrase.TIME_UNITS, Selector.THIRD)));
        }

        if (component) {
            Date logDate = new Date(resultTime * 1000L);
            String formattedTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(logDate);

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