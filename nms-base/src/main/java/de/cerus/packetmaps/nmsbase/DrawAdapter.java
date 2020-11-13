package de.cerus.packetmaps.nmsbase;

import java.awt.image.BufferedImage;

public interface DrawAdapter {

    default void fill(final byte color) {
        this.fillRect(0, 0, 127, 127, color);
    }

    default void rect(final int x1, final int z1, final int x2, final int z2, final byte color) {
        this.line(x1, z1, x2, z1, color);
        this.line(x1, z2, x2, z2, color);
        this.line(x1, z1, x1, z2, color);
        this.line(x2, z1, x2, z2, color);
    }

    void fillRect(int x1, int z1, int x2, int z2, byte color);

    void line(int x1, int z1, int x2, int z2, byte color);

    void fillSphere(int originX, int originZ, int radius, byte color);

    void setPixel(int x, int z, byte color);

    default void drawImage(final BufferedImage image) {
        this.drawImage(image, 0, 0, image.getWidth(), image.getHeight());
    }

    void drawImage(BufferedImage image, int x, int z, int width, int height);

    DiffResult diff(DrawAdapter drawAdapter);

    byte[][] toArray();

    byte[] toSingleArray();

    class DiffResult {

        private final int minX;
        private final int minZ;
        private final int width;
        private final int height;
        private final byte[] data;

        public DiffResult(final int minX, final int minZ, final int width, final int height, final byte[] data) {
            this.minX = minX;
            this.minZ = minZ;
            this.width = width;
            this.height = height;
            this.data = data;
        }

        public int getMinX() {
            return this.minX;
        }

        public int getMinZ() {
            return this.minZ;
        }

        public int getWidth() {
            return this.width;
        }

        public int getHeight() {
            return this.height;
        }

        public byte[] getData() {
            return this.data;
        }

    }
}
