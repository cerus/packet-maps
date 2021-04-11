package de.cerus.packetmaps.core.screen;

import de.cerus.packetmaps.nmsbase.FakeMap;
import de.cerus.packetmaps.nmsbase.MapIcon;
import de.cerus.packetmaps.nmsbase.NmsAdapter;
import java.awt.Dimension;
import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * A screen spanning a rectangular area using item frames and fake maps
 */
public class MapScreen {

    private final int width;
    private final int height;
    private final Entity[][] entities;
    private final NmsAdapter nmsAdapter;
    private final ScreenGraphics screenGraphics;
    private final Map<Point, MapIcon> iconMap;
    private FakeMap[][] fakeMaps;

    public MapScreen(final int width, final int height, final Entity[][] entities, final NmsAdapter nmsAdapter) {
        this.width = width;
        this.height = height;
        this.entities = entities;
        this.nmsAdapter = nmsAdapter;
        this.screenGraphics = new ScreenGraphics(this.getTotalDimensions(), nmsAdapter);
        this.iconMap = new HashMap<>();

        this.createFakeMaps();
    }

    /**
     * Updates the cached map data
     */
    public void update() {
        final byte[] data = this.screenGraphics.getData();

        for (int col = 0; col < this.width; col++) {
            for (int row = 0; row < this.height; row++) {
                // Get fake map at col and row
                final FakeMap fakeMap = this.fakeMaps[col][row];

                // Create temporary data array
                final byte[] arr = new byte[128 * 128];
                for (int x = 0; x < 128; x++) {
                    for (int z = 0; z < 128; z++) {
                        // Translate col, row, x and z to X and Z for the graphics data array
                        final int X = (col * 128) + x;
                        final int Z = (row * 128) + z;

                        // Get color and put it into the temporary array
                        final byte color = data[X + Z * this.getTotalWidth()];
                        arr[x + z * 128] = color;

                        // Update icons
                        if (!this.iconMap.isEmpty()) {
                            final int finalX = x;
                            final int finalZ = z;
                            this.iconMap.keySet().stream()
                                    .filter(point -> point.x == X && point.y == Z)
                                    .findFirst()
                                    .ifPresent(point -> {
                                        MapIcon mapIcon = this.iconMap.get(point);

                                        // Translate coordinates
                                        final int sx = this.scale(finalX);
                                        final int sz = this.scale(finalZ);

                                        mapIcon = new MapIcon(mapIcon.getType(), sx, sz, mapIcon.getRotation(), mapIcon.getName());

                                        final List<MapIcon> icons = fakeMap.getIcons();
                                        if (!icons.contains(mapIcon)) {
                                            icons.add(mapIcon);
                                        }
                                    });
                        }
                    }
                }
                // Overwrite the fake maps cached data with the temp array
                fakeMap.setCachedData(arr);
            }
        }
    }

    /**
     * Source: https://stackoverflow.com/a/929107/10821925
     *
     * @param val The old value
     *
     * @return The new value
     */
    private int scale(final int val) {
        return (((val) * (127 - -128)) / 128) + -128;
    }

    public void sendScreenPart(final int col, final int row, final int minX, final int minZ, final int width, final int height, final byte[] data) {
        // Get frame and map at col & row
        final Entity frame = this.entities[col][row];
        final FakeMap fakeMap = this.fakeMaps[col][row];

        fakeMap.sendSlice(minX, minZ, width, height, data);
        fakeMap.getObservers().forEach(player ->
                this.nmsAdapter.sendPacket(player, this.nmsAdapter.constructFramePacket(frame.getEntityId(), fakeMap.getId())));
    }

    /**
     * Sends the maps to the observers
     */
    public void send() {
        for (int col = 0; col < this.width; col++) {
            for (int row = 0; row < this.height; row++) {
                // Get frame and map at col & row
                final Entity frame = this.entities[col][row];
                final FakeMap fakeMap = this.fakeMaps[col][row];

                // Send map and frame data
                fakeMap.sendCached();
                fakeMap.getObservers().forEach(player ->
                        this.nmsAdapter.sendPacket(player, this.nmsAdapter.constructFramePacket(frame.getEntityId(), fakeMap.getId())));
            }
        }
    }

    public void addObserver(final Player player) {
        for (final FakeMap[] arr : this.fakeMaps) {
            for (final FakeMap fakeMap : arr) {
                fakeMap.addObserver(player);
            }
        }
    }

    public void removeObserver(final Player player) {
        for (final FakeMap[] arr : this.fakeMaps) {
            for (final FakeMap fakeMap : arr) {
                fakeMap.removeObserver(player);
            }
        }
    }

    public void clearObservers() {
        for (final FakeMap[] arr : this.fakeMaps) {
            for (final FakeMap fakeMap : arr) {
                fakeMap.getObservers().forEach(fakeMap::removeObserver);
            }
        }
    }

    public void addIcon(final MapIcon mapIcon) {
        final Point point = new Point(mapIcon.getX(), mapIcon.getY());
        this.iconMap.put(point, mapIcon);
    }

    /**
     * Creates all the different fake maps for the screen
     */
    private void createFakeMaps() {
        this.fakeMaps = new FakeMap[this.width][this.height];

        for (int x = 0; x < this.width; x++) {
            for (int z = 0; z < this.height; z++) {
                final FakeMap fakeMap = this.fakeMaps[x][z] = new FakeMap(this.nmsAdapter, null);
                fakeMap.setTrackPos(true);
                fakeMap.setCachedData(new byte[128 * 128]);
            }
        }
    }

    public boolean isValid() {
        for (final Entity[] arr : this.entities) {
            for (final Entity entity : arr) {
                if (!entity.isValid()) {
                    return false;
                }
            }
        }
        return true;
    }

    public ScreenGraphics getScreenGraphics() {
        return this.screenGraphics;
    }

    public Dimension getTotalDimensions() {
        return new Dimension(this.getTotalWidth(), this.getTotalHeight());
    }

    public int getTotalWidth() {
        return this.width * 128;
    }

    public int getTotalHeight() {
        return this.height * 128;
    }

}
