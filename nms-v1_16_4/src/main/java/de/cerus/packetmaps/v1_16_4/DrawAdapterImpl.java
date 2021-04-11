package de.cerus.packetmaps.v1_16_4;

import de.cerus.packetmaps.nmsbase.DrawAdapter;
import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class DrawAdapterImpl implements DrawAdapter {

    private final byte[][] data = new byte[128][128];

    @Override
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

    @Override
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

    @Override
    public void setPixel(final int x, final int z, final byte color) {
        this.data[x][z] = color;
    }

    @Override
    public void drawImage(final BufferedImage image, final int x, final int z, final int width, final int height) {
        if (width <= 0 || height <= 0) {
            return;
        }

        final NmsAdapterImpl nmsAdapter = new NmsAdapterImpl();
        for (int X = 0; X < Math.min(128, width); X++) {
            for (int Z = 0; Z < Math.min(128, height); Z++) {
                final int rgb = image.getRGB(X + x, Z + z);
                final Color color = new Color(rgb);
                this.setPixel(X, Z, nmsAdapter.matchRgb(color.getRed(), color.getGreen(), color.getBlue()));
            }
        }
    }

    @Override
    public void fillSphere(final int originX, final int originZ, final int radius, final byte color) {
    }

    @Override
    public DiffResult diff(final DrawAdapter drawAdapter) {
        final byte[] thisData = this.toOneDimArray();
        final byte[] thatData = drawAdapter.toOneDimArray();
        byte[] newData = new byte[0];

        int minX = -1;
        int minZ = -1;
        int width = 0;
        int height = 0;

        // First loop: calculate width, height, minX, minZ and newData size
        for (int x = 0; x < 128; x++) {
            for (int z = 0; z < 128; z++) {
                // Get colors
                final byte thisColor = thisData[x + z * 128];
                final byte thatColor = thatData[x + z * 128];

                // Skip if the colors are the same or if the other color is transparent
                if (thisColor == thatColor || thatColor < 4 /*0-3 = transparent*/) {
                    continue;
                }

                // Set minX if it has not been touched yet
                if (minX == -1) {
                    minX = x;
                }
                // Set minZ if it has not been touched yet
                if (minZ == -1) {
                    minZ = z;
                }

                // Calculate current width and height
                final int w = (x - minX) + 1;
                final int h = (z - minZ) + 1;

                // Check if current width is greater than width
                if (w > width) {
                    width = w;

                    // Resize array
                    final byte[] arr = new byte[w * height];
                    System.arraycopy(newData, 0, arr, 0, newData.length);
                    newData = arr;
                }

                // Check if current height is greater than height
                if (h > height) {
                    height = h;

                    // Resize array
                    final byte[] arr = new byte[width * h];
                    System.arraycopy(newData, 0, arr, 0, newData.length);
                    newData = arr;
                }
            }
        }

        // Second loop: set colors
        for (int x = 0; x < 128; x++) {
            for (int z = 0; z < 128; z++) {
                // Get colors
                final byte thisColor = thisData[x + z * 128];
                final byte thatColor = thatData[x + z * 128];

                // Skip if the colors are the same or if the other color is transparent
                if (thisColor == thatColor || thatColor < 4 /*0-3 = transparent*/) {
                    continue;
                }

                // Normalize x and z
                final int normX = x - minX;
                final int normZ = z - minZ;

                // Set color
                newData[normX + normZ * width] = thatColor;
            }
        }

        // Return result
        return new DiffResult(minX, minZ, width, height, newData);
    }

    @Override
    public byte[][] toArray() {
        return this.data;
    }

    @Override
    public byte[] toOneDimArray() {
        final byte[] arr = new byte[128 * 128];
        for (int x = 0; x < 128; x++) {
            for (int z = 0; z < 128; z++) {
                arr[x + z * 128] = this.data[x][z];
            }
        }
        return arr;
    }

}
