package net.runelite.client.plugins.socket.plugins.playerstatus.marker;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.client.plugins.socket.plugins.playerstatus.gametimer.GameIndicator;
import net.runelite.client.plugins.socket.plugins.playerstatus.gametimer.GameTimer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.Instant;

@AllArgsConstructor
public class TimerMarker extends AbstractMarker {

    @Getter(AccessLevel.PUBLIC)
    private GameTimer timer;

    @Getter(AccessLevel.PUBLIC)
    private long startTime;

}
