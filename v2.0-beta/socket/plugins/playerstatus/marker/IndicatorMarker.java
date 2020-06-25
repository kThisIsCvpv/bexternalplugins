package net.runelite.client.plugins.socket.plugins.playerstatus.marker;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.client.plugins.socket.plugins.playerstatus.gametimer.GameIndicator;

import java.awt.*;
import java.awt.image.BufferedImage;

@AllArgsConstructor
public class IndicatorMarker extends AbstractMarker {

    @Getter(AccessLevel.PUBLIC)
    private GameIndicator indicator;

}
