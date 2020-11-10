package de.cerus.packetmaps.v1_16_3;

import de.cerus.packetmaps.nmsbase.DrawAdapter;
import java.awt.Point;
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

        for (int x = effectiveX1; x < effectiveX2; x++) {
            for (int z = effectiveZ1; z < effectiveZ2; z++) {
                this.data[x][z] = color;
            }
        }
    }

    @Override
    public void line(final int x1, final int z1, final int x2, final int z2, final byte color) {
        final Point a = new Point(x1, z1);
        final Point b = new Point(x2, z2);
        final List<Point> points = this.walkPoints(a, b, (int) a.distance(b.x, b.y));
        points.forEach(point -> this.data[point.y][point.x] = color);
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
    public void fillSphere(final int originX, final int originZ, final int radius, final byte color) {

    }

    @Override
    public byte[][] toArray() {
        return this.data;
    }

}
