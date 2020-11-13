package de.cerus.packetmaps.core.pmcache;

import de.cerus.packetmaps.nmsbase.NmsAdapter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.bukkit.Bukkit;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

public class VideoCache implements Cache {

    public static final int ID = 1;

    private int frameCount;
    private Dimension dimension;
    private byte[][] cachedFrames;

    VideoCache() {
    }

    public VideoCache(final int frameCount, final Dimension dimension, final byte[][] cachedFrames) {
        this.frameCount = frameCount;
        this.dimension = dimension;
        this.cachedFrames = cachedFrames;
    }

    public static VideoCache buildCache(final FrameGrab frameGrab, final NmsAdapter nmsAdapter) throws IOException {
        final List<BufferedImage> images = new ArrayList<>();
        Picture picture;
        while (null != (picture = frameGrab.getNativeFrame())) {
            images.add(AWTUtil.toBufferedImage(picture));
        }
        final int size = images.size();

        Bukkit.broadcastMessage("grabbed " + size + " frames");

        Dimension dimension = null;

        final byte[][] data = new byte[size][];
        for (int i = 0; i < size; i++) {
            Bukkit.broadcastMessage(i + "/" + size);

            final BufferedImage bufferedImage = images.remove(0);
            if (dimension == null) {
                dimension = new Dimension(bufferedImage.getWidth(), bufferedImage.getHeight());
            }

            // Create data array for current frame
            final byte[] arr = new byte[bufferedImage.getWidth() * bufferedImage.getHeight()];
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                for (int z = 0; z < bufferedImage.getHeight(); z++) {
                    // Match rgb to map color
                    final Color color = new Color(bufferedImage.getRGB(x, z));
                    final byte byteColor = nmsAdapter.matchRgb(color.getRed(), color.getGreen(), color.getBlue());

                    // Set data
                    arr[x + z * bufferedImage.getWidth()] = byteColor;
                }
            }

            data[i] = arr;
        }

        return new VideoCache(size, dimension, data);
    }

    public void resize(final Dimension newDimension) {
        if (newDimension.width < this.dimension.width || newDimension.height < this.dimension.height) {
            throw new IllegalArgumentException("Cannot make cache smaller");
        }
        if (newDimension.width == this.dimension.width && newDimension.height == this.dimension.height) {
            return;
        }

        // Create data array
        //final byte[][] newData = new byte[this.frameCount][];

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
            this.cachedFrames[i] = newCachedFrame;
        }
        //this.cachedFrames = newData;
    }

    @Override
    public void write(final DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(this.getId());

        final GZIPOutputStream gzipOut = new GZIPOutputStream(outputStream);
        final DataOutputStream dOut = new DataOutputStream(gzipOut);

        dOut.writeInt(this.frameCount);
        dOut.writeInt(this.dimension.width);
        dOut.writeInt(this.dimension.height);

        for (final byte[] cachedFrame : this.cachedFrames) {
            dOut.writeInt(cachedFrame.length);
            dOut.write(cachedFrame);
        }

        gzipOut.close();
    }

    @Override
    public void read(final DataInputStream inputStream) throws IOException {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        final byte[] buf = new byte[1024];
        int readLen;
        while ((readLen = inputStream.read(buf)) != -1) {
            bOut.write(buf, 0, readLen);
        }
        final byte[] bytes = bOut.toByteArray();

        final GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream(bytes));
        bOut = new ByteArrayOutputStream();
        while ((readLen = gzipIn.read(buf)) != -1) {
            bOut.write(buf, 0, readLen);
        }

        final DataInputStream dataIn = new DataInputStream(new ByteArrayInputStream(bOut.toByteArray()));
        this.frameCount = dataIn.readInt();
        this.dimension = new Dimension(dataIn.readInt(), dataIn.readInt());

        this.cachedFrames = new byte[this.frameCount][];
        for (int i = 0; i < this.frameCount; i++) {
            final int len = dataIn.readInt();
            final byte[] frame = new byte[len];
            final int read = dataIn.read(frame, 0, len);
            if (read != len) {
                System.out.println("Warning: Expected " + len + " bytes but got " + read);
            }

            this.cachedFrames[i] = frame;
        }
    }

    @Override
    public int getId() {
        return ID;
    }

    public Dimension getDimension() {
        return this.dimension;
    }

    public byte[][] getCachedFrames() {
        return this.cachedFrames;
    }

    public int getFrameCount() {
        return this.frameCount;
    }

}
