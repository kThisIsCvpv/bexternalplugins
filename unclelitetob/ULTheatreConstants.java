package net.runelite.client.plugins.unclelitetob;

import java.util.regex.Pattern;

public class ULTheatreConstants {

    public static final Pattern PATTERN_MAIDEN = Pattern.compile("Wave 'The Maiden of Sugadinti' complete! Duration: \\<col\\=ff0000\\>[0-9:]+\\</col\\> Total: \\<col\\=ff0000\\>[0-9:]+\\</col\\>");
    public static final String MESSAGE_MAIDEN = "Wave 'The Maiden of Sugadinti' complete! Duration: <col=ff0000>%s</col> Total: <col=ff0000>%s</col>";

    public static final Pattern PATTERN_BLOAT = Pattern.compile("Wave 'The Pestilent Bloat' complete! Duration: \\<col\\=ff0000\\>[0-9:]+\\</col\\> Total: \\<col\\=ff0000\\>[0-9:]+\\</col\\>");
    public static final String MESSAGE_BLOAT = "Wave 'The Pestilent Bloat' complete! Duration: <col=ff0000>%s</col> Total: <col=ff0000>%s</col>";

    public static final Pattern PATTERN_NYLOCAS = Pattern.compile("Wave 'The Nylocas' complete! Duration: \\<col\\=ff0000\\>[0-9:]+\\</col\\> Total: \\<col\\=ff0000\\>[0-9:]+\\</col\\>");
    public static final String MESSAGE_NYLOCAS = "Wave 'The Nylocas' complete! Duration: <col=ff0000>%s</col> Total: <col=ff0000>%s</col>";

    public static final Pattern PATTERN_SOTETSEG = Pattern.compile("Wave 'Sotetseg' complete! Duration: \\<col\\=ff0000\\>[0-9:]+\\</col\\> Total: \\<col\\=ff0000\\>[0-9:]+\\</col\\>");
    public static final String MESSAGE_SOTETSEG = "Wave 'Sotetseg' complete! Duration: <col=ff0000>%s</col> Total: <col=ff0000>%s</col>";

    public static final Pattern PATTERN_XARPUS = Pattern.compile("Wave 'Xarpus' complete! Duration: \\<col\\=ff0000\\>[0-9:]+\\</col\\> Total: \\<col\\=ff0000\\>[0-9:]+\\</col\\>");
    public static final String MESSAGE_XARPUS = "Wave 'Xarpus' complete! Duration: <col=ff0000>%s</col> Total: <col=ff0000>%s</col>";

    public static final Pattern PATTERN_VERZIK = Pattern.compile("Wave 'The Final Challenge' complete! Duration: \\<col\\=ff0000\\>[0-9:]+\\</col\\><br>Theatre of Blood wave completion time: \\<col\\=ff0000\\>[0-9:]+\\</col\\>");
    public static final String MESSAGE_VERZIK = "Wave 'The Final Challenge' complete! Duration: <col=ff0000>%s</col><br>Theatre of Blood wave completion time: <col=ff0000>%s</col>";

    public static final Pattern PATTERN_TOTAL = Pattern.compile("Theatre of Blood total completion time: \\<col\\=ff0000\\>[0-9:]+\\</col\\>");
    public static final String MESSAGE_TOTAL = "Theatre of Blood total completion time: <col=ff0000>%s</col>";

    public static final int THEATRE_WIDGET = 459;

    public static final int MAX_PLAYERS = 5;

    public static final int COMP_PLAYER_ONE_NAME = 22;
    public static final int COMP_PLAYER_ONE_DEATHS = 23;

    public static final int COMP_PLAYER_TWO_NAME = 24;
    public static final int COMP_PLAYER_TWO_DEATHS = 25;

    public static final int COMP_PLAYER_THREE_NAME = 26;
    public static final int COMP_PLAYER_THREE_DEATHS = 27;

    public static final int COMP_PLAYER_FOUR_NAME = 28;
    public static final int COMP_PLAYER_FOUR_DEATHS = 29;

    public static final int COMP_PLAYER_FIVE_NAME = 30;
    public static final int COMP_PLAYER_FIVE_DEATHS = 31;

    public static final int COMP_TOTAL_DEATHS = 33;

    public static final int COMP_CHALLENGE_TIME = 11;
    public static final int COMP_OVERALL_TIME = 36;

    public static final int COMP_MVP = 14;

    public static final int COMP_MAIDEN = 45;
    public static final int COMP_BLOAT = 47;
    public static final int COMP_NYLOCAS = 49;
    public static final int COMP_SOTETSEG = 51;
    public static final int COMP_XARPUS = 53;
    public static final int COMP_VERZIK = 55;

}
