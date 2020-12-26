package de.cerus.packetmaps.core.screen.threedim;

import de.cerus.packetmaps.core.screen.ScreenGraphics;
import de.cerus.packetmaps.core.screen.threedim.shape.Triangle;
import de.cerus.packetmaps.nmsbase.NmsAdapter;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ScreenGraphics3D {

    private final ScreenGraphics projector;
    private final Dimension screenDimension;
    private final NmsAdapter nmsAdapter;
    private final Map<Color, Byte> cachedColors = new HashMap<>();

    public ScreenGraphics3D(final ScreenGraphics projector, final NmsAdapter nmsAdapter) {
        this.projector = projector;
        this.screenDimension = projector.getScreenDimension();
        this.nmsAdapter = nmsAdapter;
    }

    public void setPixel(final int x, final int z, final byte color) {
        if (x < 0 || x > this.screenDimension.width || z < 0 || z > this.screenDimension.height) {
            return;
        }

        this.projector.setPixel(x, z, color);
    }

    public void renderTriangles(final Matrix3 transform, final Triangle... shapes) {
        final double[] zBuffer = new double[this.screenDimension.width * this.screenDimension.height];
        // initialize array with extremely far away depths
        Arrays.fill(zBuffer, Double.NEGATIVE_INFINITY);

        for (final Triangle shape : shapes) {
            final Vertex v1 = transform.transform(shape.getV1());
            final Vertex v2 = transform.transform(shape.getV2());
            final Vertex v3 = transform.transform(shape.getV3());

            // Temporary translations
            v1.x += this.screenDimension.width / 2d;
            v1.y += this.screenDimension.height / 2d;
            v2.x += this.screenDimension.width / 2d;
            v2.y += this.screenDimension.height / 2d;
            v3.x += this.screenDimension.width / 2d;
            v3.y += this.screenDimension.height / 2d;

            // compute rectangular bounds for triangle
            final int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
            final int maxX = (int) Math.min(this.screenDimension.width - 1, Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));
            final int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
            final int maxY = (int) Math.min(this.screenDimension.height - 1, Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));

            final double triangleArea = (v1.y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - v1.x);

            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {
                    final double b1 = ((y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - x)) / triangleArea;
                    final double b2 = ((y - v1.y) * (v3.x - v1.x) + (v3.y - v1.y) * (v1.x - x)) / triangleArea;
                    final double b3 = ((y - v2.y) * (v1.x - v2.x) + (v1.y - v2.y) * (v2.x - x)) / triangleArea;

                    if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
                        final double depth = b1 * v1.z + b2 * v2.z + b3 * v3.z;
                        final int zIndex = y * this.screenDimension.width + x;

                        if (zBuffer[zIndex] < depth) {
                            final Color color = shape.getColor();
                            final byte clr = this.colorCache(color);
                            this.projector.setPixel(x, y, this.nmsAdapter.matchRgb(color.getRed(), color.getGreen(), color.getBlue()));
                            zBuffer[zIndex] = depth;
                        }
                    }
                }
            }

        }
    }

    private byte colorCache(final Color color) {
        if (this.cachedColors.containsKey(color)) {
            return this.cachedColors.get(color);
        }
        this.cachedColors.put(color, this.nmsAdapter.matchRgb(color.getRed(), color.getGreen(), color.getBlue()));
        return this.colorCache(color);
    }

}
