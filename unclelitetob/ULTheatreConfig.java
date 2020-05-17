package net.runelite.client.plugins.unclelitetob;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("UncleLite+ Theatre Config")
public interface ULTheatreConfig extends Config {

    @ConfigItem(
            position = 0,
            keyName = "getPlayerOneName",
            name = "Player 1 Name",
            description = "Player 1 Name."
    )
    default String getPlayerOneName() {
        return "Yuri-chan";
    }

    @ConfigItem(
            position = 1,
            keyName = "getPlayerOneDeaths",
            name = "Player 1 Deaths",
            description = "Player 1 Deaths. Set to < 0 to disable."
    )
    default int getPlayerOneDeaths() {
        return 0;
    }

    @ConfigItem(
            position = 2,
            keyName = "getPlayerTwoName",
            name = "Player 2 Name",
            description = "Player 2 Name."
    )
    default String getPlayerTwoName() {
        return "invy tags";
    }

    @ConfigItem(
            position = 3,
            keyName = "getPlayerTwoDeaths",
            name = "Player 2 Deaths",
            description = "Player 2 Deaths. Set to < 0 to disable."
    )
    default int getPlayerTwoDeaths() {
        return 0;
    }

    @ConfigItem(
            position = 4,
            keyName = "getPlayerThreeName",
            name = "Player 3 Name",
            description = "Player 3 Name."
    )
    default String getPlayerThreeName() {
        return "Pretz Jr";
    }

    @ConfigItem(
            position = 5,
            keyName = "getPlayerThreeDeaths",
            name = "Player 3 Deaths",
            description = "Player 3 Deaths. Set to < 0 to disable."
    )
    default int getPlayerThreeDeaths() {
        return 0;
    }

    @ConfigItem(
            position = 6,
            keyName = "getPlayerFourName",
            name = "Player 4 Name",
            description = "Player 4 Name."
    )
    default String getPlayerFourName() {
        return "ewenn";
    }

    @ConfigItem(
            position = 7,
            keyName = "getPlayerFourDeaths",
            name = "Player 4 Deaths",
            description = "Player 4 Deaths. Set to < 0 to disable."
    )
    default int getPlayerFourDeaths() {
        return 0;
    }

    @ConfigItem(
            position = 8,
            keyName = "getPlayerFiveName",
            name = "Player 5 Name",
            description = "Player 5 Name."
    )
    default String getPlayerFiveName() {
        return "Nyan-pasu";
    }

    @ConfigItem(
            position = 9,
            keyName = "getPlayerFiveDeaths",
            name = "Player 5 Deaths",
            description = "Player 5 Deaths. Set to < 0 to disable."
    )
    default int getPlayerFiveDeaths() {
        return 0;
    }

    @ConfigItem(
            position = 10,
            keyName = "getMVP",
            name = "Most Valuable Player",
            description = "The most valuable player of this raid team."
    )
    default String getMVP() {
        return "Yuri-chan";
    }

    @ConfigItem(
            position = 11,
            keyName = "getMaidenTime",
            name = "Maiden Time",
            description = "The time (in seconds) it took to complete The Maiden of Sugadinti."
    )
    default int getMaidenTime() {
        return 77;
    }

    @ConfigItem(
            position = 12,
            keyName = "getBloatTime",
            name = "Bloat Time",
            description = "The time (in seconds) it took to complete The Pestilent Bloat."
    )
    default int getBloatTime() {
        return 44;
    }

    @ConfigItem(
            position = 13,
            keyName = "getNylocasTime",
            name = "Nylocas Time",
            description = "The time (in seconds) it took to complete The Nylocas."
    )
    default int getNylocasTime() {
        return 214;
    }

    @ConfigItem(
            position = 14,
            keyName = "getSotetsegTime",
            name = "Sotetseg Time",
            description = "The time (in seconds) it took to complete Sotetseg."
    )
    default int getSotetsegTime() {
        return 102;
    }

    @ConfigItem(
            position = 15,
            keyName = "getXarpusTime",
            name = "Xarpus Time",
            description = "The time (in seconds) it took to complete Xarpus."
    )
    default int getXarpusTime() {
        return 135;
    }

    @ConfigItem(
            position = 16,
            keyName = "getVerzikTime",
            name = "Verzik Time",
            description = "The time (in seconds) it took to complete The Final Challenge."
    )
    default int getVerzikTime() {
        return 220;
    }

    @ConfigItem(
            position = 17,
            keyName = "getPrepTime",
            name = "Prep Time",
            description = "The time (in seconds) it took between the episodes."
    )
    default int getPrepTime() {
        return 484;
    }
}
