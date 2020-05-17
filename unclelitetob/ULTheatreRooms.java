package net.runelite.client.plugins.unclelitetob;

import lombok.AccessLevel;
import lombok.Getter;

import java.util.regex.Pattern;

import static net.runelite.client.plugins.unclelitetob.ULTheatreConstants.*;

public enum ULTheatreRooms {

    MAIDEN(PATTERN_MAIDEN, MESSAGE_MAIDEN, 0),
    BLOAT(PATTERN_BLOAT, MESSAGE_BLOAT, 1),
    NYLOCAS(PATTERN_NYLOCAS, MESSAGE_NYLOCAS, 2),
    SOTETSEG(PATTERN_SOTETSEG, MESSAGE_SOTETSEG, 3),
    XARPUS(PATTERN_XARPUS, MESSAGE_XARPUS, 4),
    VERZIK(PATTERN_VERZIK, MESSAGE_VERZIK, 5);

    @Getter(AccessLevel.PUBLIC)
    private Pattern pattern;

    @Getter(AccessLevel.PUBLIC)
    private String message;

    @Getter(AccessLevel.PUBLIC)
    private int index;

    ULTheatreRooms(Pattern pattern, String message, int index) {
        this.pattern = pattern;
        this.message = message;
        this.index = index;
    }

}
