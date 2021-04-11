package de.cerus.packetmaps.core.screen;

import de.cerus.packetmaps.nmsbase.NmsAdapter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.map.MapFont;
import org.bukkit.map.MinecraftFont;

public class ScreenGraphics {

    private final Dimension screenDimension;
    private final NmsAdapter nmsAdapter;
    private byte[] data;

    public ScreenGraphics(final Dimension screenDimension, final NmsAdapter nmsAdapter) {
        this.screenDimension = screenDimension;
        this.data = new byte[screenDimension.width * screenDimension.height];
        this.nmsAdapter = nmsAdapter;
    }

    /**
     * Fills an area with a specific color
     *
     * @param x1    The x coordinate of the first corner
     * @param z1    The z coordinate of the first corner
     * @param x2    The x coordinate of the second corner
     * @param z2    The z coordinate of the second corner
     * @param color The color of the rectangle
     */
    public void fillRect(final int x1, final int z1, final int x2, final int z2, final byte color) {
        final int effectiveX1 = Math.min(x1, x2);
        final int effectiveX2 = Math.max(x1, x2);
        final int effectiveZ1 = Math.min(z1, z2);
        final int effectiveZ2 = Math.max(z1, z2);

        for (int x = effectiveX1; x < effectiveX2 + 1; x++) {
            for (int z = effectiveZ1; z < effectiveZ2 + 1; z++) {
                this.setPixel(x, z, color);
            }
        }
    }

    /**
     * Draws a line from one coordinate to the other
     *
     * @param x1    The first x coordinate
     * @param z1    The first z coordinate
     * @param x2    The second x coordinate
     * @param z2    The second z coordinate
     * @param color The color of the line
     */
    public void line(final int x1, final int z1, final int x2, final int z2, final byte color) {
        final Point a = new Point(x1, z1);
        final Point b = new Point(x2, z2);

        final int distance = (int) Math.round(a.distance(b.x, b.y)) + 1;
        final List<Point> points = this.walkPoints(a, b, distance);
        points.forEach(point -> this.setPixel(point.x, point.y, color));
    }

    /**
     * Calculates points between two points
     * Source: https://stackoverflow.com/a/34142336/10821925
     *
     * @param a     First point
     * @param b     Second point
     * @param count The amount of points that you want
     *
     * @return n (count) points between a and b
     */
    private List<Point> walkPoints(final Point a, final Point b, final int count) {
        final List<Point> points = new ArrayList<>();

        final int yDiff = b.y - a.y;
        final int xDiff = b.x - a.x;
        final double slope = (double) (b.y - a.y) / (b.x - a.x);
        double x, y;

        int qty = count;
        --qty;

        for (double i = 0; i < qty; i++) {
            y = slope == 0 ? 0 : yDiff * (i / qty);
            x = slope == 0 ? xDiff * (i / qty) : y / slope;
            points.add(new Point((int) Math.round(x) + a.x, (int) Math.round(y) + a.y));
        }
        points.add(b);

        return points;
    }

    /**
     * Draws a part of an image at the specified coordinates
     * If the specified width or height with exceed the bounds of the screen the image will be truncated.
     *
     * @param image   The image
     * @param sourceX The x coordinate from where the image should be drawn
     * @param sourceZ The z coordinate from where the image should be drawn
     * @param destX   The x coordinate on the screen
     * @param destZ   The z coordinate on the screen
     * @param width   The amount of pixels that should be drawn on the x axis
     * @param height  The amount of pixels that should be drawn on the z axis
     */
    public void drawImage(final BufferedImage image, final int sourceX, final int sourceZ, final int destX, final int destZ, final int width, final int height) {
        if (width <= 0 || height <= 0) {
            return;
        }

        for (int X = 0; X < Math.min(this.screenDimension.width, width); X++) {
            for (int Z = 0; Z < Math.min(this.screenDimension.height, height); Z++) {
                final int rgb = image.getRGB(X + sourceX, Z + sourceZ);
                final Color color = new Color(rgb);
                this.setPixel(X + destX, Z + destZ, this.nmsAdapter.matchRgb(color.getRed(), color.getGreen(), color.getBlue()));
            }
        }
    }

    /**
     * Draws text
     * Source: Craftbukkits CraftMapCanvas
     *
     * @param x    x coordinate
     * @param z    z coordinate
     * @param text The text
     */
    public void drawText(int x, int z, final String text, final byte startColor) {
        final MapFont font = MinecraftFont.Font;

        final int xStart = x;
        byte color = startColor;
        if (!font.isValid(text)) {
            throw new IllegalArgumentException("text contains invalid characters");
        } else {
            int currentIndex = 0;

            while (true) {
                if (currentIndex >= text.length()) {
                    return;
                }

                final char ch = text.charAt(currentIndex);
                if (ch == '\n') {
                    // Increment z if the char is a line separator
                    x = xStart;
                    z += font.getHeight() + 1;
                } else if (ch == '\u00A7' /*-> §*/) {
                    // Get distance from current char to end char (';')
                    final int end = text.indexOf(';', currentIndex);
                    if (end < 0) {
                        break;
                    }

                    // Parse color
                    try {
                        color = Byte.parseByte(text.substring(currentIndex + 1, end));
                        currentIndex = end;
                    } catch (final NumberFormatException var12) {
                        break;
                    }
                } else {
                    // Draw text if the character is not a special character
                    final MapFont.CharacterSprite sprite = font.getChar(text.charAt(currentIndex));

                    for (int row = 0; row < font.getHeight(); ++row) {
                        for (int col = 0; col < sprite.getWidth(); ++col) {
                            if (sprite.get(row, col)) {
                                this.setPixel(x + col, z + row, color);
                            }
                        }
                    }

                    // Increment x
                    x += sprite.getWidth() + 1;
                }

                ++currentIndex;
            }

            throw new IllegalArgumentException("Text contains unterminated color string");
        }
    }

    /**
     * Sets a specific pixel
     *
     * @param x     The x coordinate
     * @param z     The z coordinate
     * @param color The color
     */
    public void setPixel(final int x, final int z, final byte color) {
        if (x >= this.screenDimension.width || z >= this.screenDimension.height) {
            // Abort because its out of bounds
            return;
        }

        this.data[x + z * this.screenDimension.width] = color;
    }

    @Override
    public ScreenGraphics clone() {
        return new ScreenGraphics(this.screenDimension, this.nmsAdapter);
    }

    public void clearData() {
        this.data = new byte[this.screenDimension.width * this.screenDimension.height];
    }

    public byte[] getData() {
        return this.data;
    }

    public void setData(final byte[] data) {
        this.data = data;
    }

    public Dimension getScreenDimension() {
        return this.screenDimension;
    }

}
