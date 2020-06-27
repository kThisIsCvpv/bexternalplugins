package net.runelite.client.plugins.socket.plugins.playerstatus;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("Socket Player Status Config v2")
public interface PlayerStatusConfig extends Config {

    @ConfigItem(
            position = 0,
            keyName = "getFontSize",
            name = "Indicator Font Size",
            description = "This is the size of the indicator and it's containing text."
    )
    default int getIndicatorSize() {
        return 15;
    }

    @ConfigItem(
            position = 1,
            keyName = "getIndicatorXOffset",
            name = "Indicator X Offset",
            description = "This is horizontal offset of the indicators."
    )
    default int getIndicatorXOffset() {
        return 5;
    }

    @ConfigItem(
            position = 2,
            keyName = "getIndicatorPadding",
            name = "Indicator Border Padding",
            description = "This is the border around each indicator entry."
    )
    default int getIndicatorPadding() {
        return 3;
    }

    @ConfigItem(
            position = 3,
            keyName = "showVengeance",
            name = "Show Vengeance Cooldown",
            description = "Show players who have vengeance on cooldown."
    )
    default boolean showVengeanceCooldown() {
        return false;
    }

    @ConfigItem(
            position = 4,
            keyName = "showVengeanceActive",
            name = "Show Vengeance Active",
            description = "Show players with an active vengeance."
    )
    default boolean showVengeanceActive() {
        return false;
    }

    @ConfigItem(
            position = 5,
            keyName = "showStamina",
            name = "Show Stamina",
            description = "Show players who drank a stamina."
    )
    default boolean showStamina() {
        return false;
    }

    @ConfigItem(
            position = 6,
            keyName = "showOverload",
            name = "Show Overload",
            description = "Show players who drank an overload."
    )
    default boolean showOverload() {
        return true;
    }

    @ConfigItem(
            position = 7,
            keyName = "showPrayerEnhance",
            name = "Show Prayer Enhance",
            description = "Show players who drank a prayer enhance."
    )
    default boolean showPrayerEnhance() {
        return false;
    }

    @ConfigItem(
            position = 8,
            keyName = "showImbuedHeart",
            name = "Show Imbued Heart",
            description = "Show players who invigorated their imbued heart."
    )
    default boolean showImbuedHeart() {
        return false;
    }

    @ConfigItem(
            position = 9,
            keyName = "getStatsRefreshRate",
            name = "Stats Refresh Rate",
            description = "The amount of ticks to wait in-between each stat request. I wouldn't touch this variable."
    )
    default int getStatsRefreshRate() {
        return 3;
    }

    @ConfigItem(
            position = 10,
            keyName = "showPlayerHealth",
            name = "Show Team Health",
            description = "Show player's health level."
    )
    default boolean showPlayerHealth() {
        return true;
    }

    @ConfigItem(
            position = 11,
            keyName = "showPlayerPrayer",
            name = "Show Team Prayer",
            description = "Show player's prayer level."
    )
    default boolean showPlayerPrayer() {
        return false;
    }

    @ConfigItem(
            position = 12,
            keyName = "showPlayerRunEnergy",
            name = "Show Team Run Energy",
            description = "Show player's run energy level."
    )
    default boolean showPlayerRunEnergy() {
        return false;
    }

    @ConfigItem(
            position = 13,
            keyName = "showPlayerSpecial",
            name = "Show Team Special",
            description = "Show player's special attack bar level."
    )
    default boolean showPlayerSpecial() {
        return false;
    }
}
