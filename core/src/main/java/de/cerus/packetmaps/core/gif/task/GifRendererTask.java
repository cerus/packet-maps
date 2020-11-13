package de.cerus.packetmaps.core.gif.task;

import de.cerus.jgif.GifImage;
import de.cerus.packetmaps.core.pmcache.GifCache;
import de.cerus.packetmaps.nmsbase.NmsAdapter;
import de.cerus.packetmaps.nmsbase.screen.MapScreen;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

public class GifRendererTask implements Runnable {

    private final MapScreen mapScreen;
    boolean cached = false;
    boolean isCaching = false;
    private GifCache gifCache;
    private int frameIndex = 0;

    public GifRendererTask(final MapScreen mapScreen, final GifCache gifCache) {
        this.mapScreen = mapScreen;
        this.gifCache = gifCache;
    }

    public GifRendererTask(final MapScreen mapScreen, final GifImage gifImage, final NmsAdapter nmsAdapter) {
        this.mapScreen = mapScreen;
        this.cache(gifImage, nmsAdapter);
    }

    public void cache(final GifImage gifImage, final NmsAdapter nmsAdapter) {
        final BufferedImage frame = gifImage.getFrame(0);
        this.gifCache = GifCache.buildCache(gifImage, new Dimension(frame.getWidth(), frame.getHeight()), nmsAdapter);
        this.gifCache.resize(this.mapScreen.getTotalDimensions());
    }

    @Override
    public void run() {
        if (this.frameIndex >= this.gifCache.getFrameCount()) {
            this.frameIndex = 0;
        }

        final byte[] cachedFrame = this.gifCache.getCachedFrame(this.frameIndex);
        this.mapScreen.getScreenGraphics().setData(cachedFrame);

        this.mapScreen.update();
        this.mapScreen.send();

        this.frameIndex++;
    }

}
