package net.runelite.client.plugins.socket.plugins.playerstatus.marker;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class AbstractMarker {

    @Setter(AccessLevel.PUBLIC)
    @Getter(AccessLevel.PUBLIC)
    private BufferedImage baseImage;

    public BufferedImage getImage(int size) {
        BufferedImage baseImage = this.getBaseImage();
        if (baseImage == null)
            return null;

        double height = baseImage.getHeight() > 0 ? baseImage.getHeight() : 1;
        double scale = ((double) size) / height;

        int newWidth = (int) Math.ceil(scale * baseImage.getWidth());
        BufferedImage realImage = new BufferedImage(newWidth, size, baseImage.getType());

        Graphics2D g2d = realImage.createGraphics();
        g2d.drawImage(baseImage, 0, 0, newWidth, size, null);
        g2d.dispose();

        return realImage;
    }
}
