package net.runelite.client.plugins.socket.plugins.playerstatus;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("Socket Player Status Config")
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
        return true;
    }

    @ConfigItem(
            position = 4,
            keyName = "showVengeanceActive",
            name = "Show Vengeance Active",
            description = "Show players with an active vengeance."
    )
    default boolean showVengeanceActive() {
        return true;
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
}
