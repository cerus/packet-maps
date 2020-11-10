package de.cerus.packetmaps.v1_16_3;

public class f {

    public static void main(final String[] args) {
        final NmsAdapterImpl nmsAdapter = new NmsAdapterImpl();

        final DrawAdapterImpl adapter = new DrawAdapterImpl();
        adapter.line(10, 0, 127, 127, nmsAdapter.matchRgb(0, 0, 0));
        adapter.line(0, 120, 127, 53, nmsAdapter.matchRgb(0, 0, 0));
        final byte[][] data = adapter.toArray();

        for (int x = 0; x < 128; x++) {
            for (int z = 0; z < 128; z++) {
                final byte b = data[x][z];

                if (b == 0) {
                    System.out.print("-");
                } else {
                    System.out.print("x");
                }
            }
            System.out.println();
        }
    }

}
