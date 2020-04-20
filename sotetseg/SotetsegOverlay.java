package net.runelite.client.plugins.sotetseg;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.*;

public class SotetsegOverlay extends Overlay {

    private final Client client;
    private final SotetsegPlugin plugin;
    private final SotetsegConfig config;

    @Inject
    private SotetsegOverlay(Client client, SotetsegPlugin plugin, SotetsegConfig config) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;

        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGH);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }


    @Override
    public Dimension render(Graphics2D graphics) {
        if (this.plugin.isSotetsegActive()) {
            for (final WorldPoint next : this.plugin.getMazePings()) {
                final LocalPoint localPoint = LocalPoint.fromWorld(this.client, next);
                if (localPoint != null) {
                    final Polygon poly = Perspective.getCanvasTilePoly(this.client, localPoint);
                    if (poly == null)
                        continue;

                    final Color color = this.config.getMazeColor();
                    OverlayUtil.renderPolygon(graphics, poly, color);
                }
            }
        }

        return null;
    }
}
