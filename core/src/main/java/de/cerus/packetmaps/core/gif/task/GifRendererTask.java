package de.cerus.packetmaps.core.gif.task;

import de.cerus.packetmaps.core.pmcache.FramedCache;
import de.cerus.packetmaps.core.screen.MapScreen;

public class GifRendererTask implements Runnable {

    private final MapScreen mapScreen;
    private final FramedCache gifCache;
    private int frameIndex = 0;

    public GifRendererTask(final MapScreen mapScreen, final FramedCache gifCache) {
        this.mapScreen = mapScreen;
        this.gifCache = gifCache;
    }

    @Override
    public void run() {
        if (this.frameIndex >= this.gifCache.getFrameCount()) {
            this.frameIndex = 0;
        }

        final byte[] cachedFrame = this.gifCache.getCachedFrames()[this.frameIndex];
        this.mapScreen.getScreenGraphics().setData(cachedFrame);

        this.mapScreen.update();
        this.mapScreen.send();

        this.frameIndex++;
    }

}
