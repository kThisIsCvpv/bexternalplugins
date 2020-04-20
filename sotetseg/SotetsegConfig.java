package net.runelite.client.plugins.sotetseg;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;
import java.util.UUID;

@ConfigGroup("SotetsegPlugin")
public interface SotetsegConfig extends Config {

    @ConfigItem(
            position = 0,
            keyName = "getMazeColor",
            name = "Maze Color",
            description = "The color of the maze being sent and drawn."
    )
    default Color getMazeColor() {
        return Color.GREEN;
    }
}
