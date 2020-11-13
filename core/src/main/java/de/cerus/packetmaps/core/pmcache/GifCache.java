package de.cerus.packetmaps.core.pmcache;

import de.cerus.jgif.GifImage;
import de.cerus.packetmaps.nmsbase.NmsAdapter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.bukkit.Bukkit;

public class GifCache implements Cache {

    public static final int ID = 0;

    private int frameCount;
    private Dimension dimension;
    private byte[][] cachedFrames;

    GifCache() {
    }

    public GifCache(final byte[][] cachedFrames, final Dimension dimension) {
        this.frameCount = cachedFrames.length;
        this.cachedFrames = cachedFrames;
        this.dimension = dimension;
    }

    /**
     * Creates a cache of the gif
     *
     * @param gifImage   The gif
     * @param dimension  The dimension
     * @param nmsAdapter The nms adapter
     *
     * @return The cache
     */
    public static GifCache buildCache(final GifImage gifImage, final Dimension dimension, final NmsAdapter nmsAdapter) {
        // Create data array
        final byte[][] data = new byte[gifImage.getFrames().size()][];

        // Loop through frames
        for (int i = 0; i < gifImage.getFrames().size(); i++) {
            Bukkit.broadcastMessage(i + "/" + gifImage.getFrames().size());

            final BufferedImage frame = gifImage.getFrame(i);
            if (frame.getWidth() > dimension.width || frame.getHeight() > dimension.height) {
                throw new IllegalStateException("Frame is too big");
            }

            // Create data array for current frame
            final byte[] arr = new byte[dimension.width * dimension.height];
            for (int x = 0; x < dimension.width; x++) {
                for (int z = 0; z < dimension.height; z++) {
                    // Match rgb to map color
                    final Color color = new Color(frame.getRGB(x, z));
                    final byte byteColor = nmsAdapter.matchRgb(color.getRed(), color.getGreen(), color.getBlue());

                    // Set data
                    arr[x + z * dimension.width] = byteColor;
                }
            }

            // Put frame array into data array
            data[i] = arr;
        }

        // Return cache
        return new GifCache(data, dimension);
    }

    @Override
    public void read(final DataInputStream inputStream) throws IOException {
        final int frameCount = inputStream.readInt(); // Read frame count
        final int width = inputStream.readInt(); // Read width
        final int height = inputStream.readInt(); // Read height
        final Dimension dimension = new Dimension(width, height);

        System.out.println("framecount is " + frameCount);
        System.out.println("width is " + width);
        System.out.println("height is " + height);

        final byte[][] data = new byte[frameCount][];
        for (int i = 0; i < frameCount; i++) {
            final byte[] cachedFrame = new byte[inputStream.readInt()];
            final int read = inputStream.read(cachedFrame, 0, cachedFrame.length);
            data[i] = cachedFrame;
            System.out.println("frame len at " + i + " is " + cachedFrame.length + " and last is " + cachedFrame[cachedFrame.length - 1]);

            if (read != cachedFrame.length) {
                System.out.println("Warning: Expected " + cachedFrame.length + " bytes but got " + read);
            }
        }

        this.frameCount = frameCount;
        this.dimension = dimension;
        this.cachedFrames = data;
    }

    /**
     * Resizes the cache
     * Warning: Caches can only be made bigger! Making a cache smaller will *not* work.
     *
     * @param newDimension The new dimension
     */
    public void resize(final Dimension newDimension) {
        if (newDimension.width < this.dimension.width || newDimension.height < this.dimension.height) {
            throw new IllegalArgumentException("Cannot make cache smaller");
        }
        if (newDimension.width == this.dimension.width && newDimension.height == this.dimension.height) {
            return;
        }

        // Create data array
        final byte[][] newData = new byte[this.frameCount][];

        // Loop through cached frames
        for (int i = 0; i < this.frameCount; i++) {
            // Get cached frame and create new array
            final byte[] cachedFrame = this.cachedFrames[i];
            final byte[] newCachedFrame = new byte[newDimension.width * newDimension.height];

            // Calculate x and z shift
            final int xShift = (newDimension.width - this.dimension.width) / 2;
            final int zShift = (newDimension.height - this.dimension.height) / 2;

            // Loop through every color data on the old frame
            for (int x = 0; x < this.dimension.width; x++) {
                for (int z = 0; z < this.dimension.height; z++) {
                    // Put the color data at the right position in the new array
                    newCachedFrame[(x + xShift) + (z + zShift) * newDimension.width] = cachedFrame[x + z * this.dimension.width];
                }
            }

            // Put frame array into data array
            newData[i] = newCachedFrame;
        }
        this.cachedFrames = newData;
    }

    @Override
    public void write(final DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(this.getId()); // Write type
        outputStream.writeInt(this.frameCount); // Write frame count
        outputStream.writeInt(this.dimension.width); // Write width
        outputStream.writeInt(this.dimension.height); // Write height

        for (int i = 0; i < this.frameCount; i++) {
            final byte[] cachedFrame = this.cachedFrames[i];
            outputStream.writeInt(cachedFrame.length);
            outputStream.write(cachedFrame);
            System.out.println("frame len at " + i + " is " + cachedFrame.length + " and last is " + cachedFrame[cachedFrame.length - 1]);
        }
    }

    public byte[] getCachedFrame(final int n) {
        if (n > this.frameCount) {
            throw new IllegalArgumentException("Invalid frame (" + n + " > " + this.frameCount + ")");
        }
        return this.cachedFrames[n];
    }

    @Override
    public int getId() {
        return ID;
    }

    public int getFrameCount() {
        return this.frameCount;
    }

    public Dimension getDimension() {
        return this.dimension;
    }

}
