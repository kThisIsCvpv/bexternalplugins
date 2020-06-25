package net.runelite.client.plugins.socket.plugins.sotetseg;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;
import java.util.UUID;

@ConfigGroup("Socket Sotetseg Config")
public interface SotetsegConfig extends Config {

    @ConfigItem(
            position = 0,
            keyName = "getTileColor",
            name = "Tile Color",
            description = "The color of the tiles."
    )
    default Color getTileColor() {
        return new Color(0, 0, 0);
    }

    @ConfigItem(
            position = 1,
            keyName = "getTileTransparency",
            name = "Tile Transparency",
            description = "The color transparency of the tiles. Ranges from 0 to 255, inclusive."
    )
    default int getTileTransparency() {
        return 50;
    }

    @ConfigItem(
            position = 2,
            keyName = "getTileOutline",
            name = "Tile Outline Color",
            description = "The color of the outline of the tiles."
    )
    default Color getTileOutline() {
        return Color.GREEN;
    }

    @ConfigItem(
            position = 3,
            keyName = "getTileOutlineSize",
            name = "Tile Outline Size",
            description = "The size of the outline of the tiles."
    )
    default int getTileOutlineSize() {
        return 1;
    }
}
