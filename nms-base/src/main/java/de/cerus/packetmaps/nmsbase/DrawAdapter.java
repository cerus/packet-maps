package de.cerus.packetmaps.nmsbase;

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

    byte[][] toArray();

}
