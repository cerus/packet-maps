package de.cerus.packetmaps.plugintools;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.util.Vector;

public class ItemFrameUtil {

    private ItemFrameUtil() {
    }

    /**
     * Returns a item frame grid that can be used for the MapScreen.
     *
     * @param startingFrame The lowest and leftmost frame
     * @param facing        The block face that the starting frame is facing
     * @param widthCap      The maximum allowed width
     * @param heightCap     The maximum allowed height
     *
     * @return A frame result
     */
    public static ItemFrameGridResult getItemFrameGrid(final ItemFrame startingFrame, final BlockFace facing, final int widthCap, final int heightCap) {
        final List<ItemFrame> list = new ArrayList<>();

        // Determine the correct vectors for the corresponding facing direction
        final Vector widthVector;
        final Vector heightVector;
        switch (facing) {
            case NORTH:
                widthVector = new Vector(-1, 0, 0);
                heightVector = new Vector(0, 1, 0);
                break;
            case EAST:
                widthVector = new Vector(0, 0, -1);
                heightVector = new Vector(0, 1, 0);
                break;
            case SOUTH:
                widthVector = new Vector(1, 0, 0);
                heightVector = new Vector(0, 1, 0);
                break;
            case WEST:
                widthVector = new Vector(0, 0, 1);
                heightVector = new Vector(0, 1, 0);
                break;
            default:
                return new ItemFrameGridResult(list, new ItemFrame[0][0], 0, 0);
        }
        list.add(startingFrame);

        final Location startingLocation = startingFrame.getLocation();
        final World world = startingLocation.getWorld();

        // Go up until there are no item frames anymore to determine the height
        int height = 1;
        while (true) {
            final Location location = startingLocation.clone().add(heightVector.clone().multiply(height));
            final ItemFrame itemFrame = getItemFrameAt(world, location.getBlockX(), location.getBlockY(), location.getBlockZ(), facing);
            if (itemFrame == null || height >= heightCap) {
                // No item frame found or cap reached, the height is determined
                break;
            }

            list.add(itemFrame);
            height++;
        }

        int width = 1;
        loop:
        while (true) {
            // For i in 0 -> height..
            // 1) ..construct a location: Take the current location, add i to the Y axis and add the direction vector multiplied by the width
            // 2) ..get a frame at the newly constructed location (if frame does not exist break the loop because we reached the end)
            // 3) ..add the frame to our collection of grid frames

            for (int i = 0; i < height; i++) {
                final Location location = startingLocation.clone().add(widthVector.clone().multiply(width))
                        .add(i == 0 ? new Vector(0, 0, 0) : heightVector.clone().multiply(i));
                final ItemFrame itemFrame = getItemFrameAt(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), facing);
                if (itemFrame == null) {
                    break loop;
                }

                list.add(itemFrame);
            }

            // Increment the width and terminate the loop if we reached the height cap
            width++;
            if (width >= widthCap) {
                break;
            }
        }

        // Make a 2d array from our frame list
        // We could do this the primitive way by creating a counter and incrementing it every loop (see below) but
        // that would not be the correct format for the map screen. Because this method was specifically designed for
        // the map screen we need to come up with some incredible formula to calculate the correct list index:
        // (height - z - 1) + (height * x)
        //
        // Primitive way:
        // int n = 0;
        // for (int x = 0; x < width; x++) {
        //     for (int z = 0; z < height; z++) {
        //         frames[x][z] = list.get(n++);
        //     }
        // }
        final ItemFrame[][] frames = new ItemFrame[width][height];
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < height; z++) {
                frames[x][z] = list.get((height - z - 1) + (height * x));
            }
        }

        return new ItemFrameGridResult(list, frames, width, height);
    }

    public static ItemFrame getItemFrameAt(final World world, final int x, final int y, final int z, final BlockFace facing) {
        final Chunk chunk = new Location(world, x, y, z).getChunk();
        for (final Entity entity : chunk.getEntities()) {
            if (entity instanceof ItemFrame && entity.getLocation().getBlockX() == x && entity.getLocation().getBlockY() == y
                    && entity.getLocation().getBlockZ() == z && entity.getFacing() == facing) {
                return (ItemFrame) entity;
            }
        }
        return null;
    }

    public static class ItemFrameGridResult {

        private final List<ItemFrame> frames;
        private final ItemFrame[][] frameGrid;
        private final int width;
        private final int height;

        public ItemFrameGridResult(final List<ItemFrame> frames, final ItemFrame[][] frameGrid, final int width, final int height) {
            this.frames = frames;
            this.frameGrid = frameGrid;
            this.width = width;
            this.height = height;
        }

        public List<ItemFrame> getFrames() {
            return this.frames;
        }

        public ItemFrame[][] getFrameGrid() {
            return this.frameGrid;
        }

        public int getWidth() {
            return this.width;
        }

        public int getHeight() {
            return this.height;
        }

    }

}
