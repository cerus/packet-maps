package de.cerus.packetmaps.nmsbase;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;

public class FakeMap {

    public static int NEXT_ID = 100_000;

    protected int id;
    protected byte scale;
    protected boolean trackPos;
    protected boolean locked;
    protected List<MapIcon> icons;
    protected DrawAdapter data;
    protected NmsAdapter nmsAdapter;
    protected List<WeakReference<Player>> observers;
    protected SliceData latestSlice;
    private byte[] cachedData;

    public FakeMap(final NmsAdapter nmsAdapter, final DrawAdapter drawAdapter) {
        this.nmsAdapter = nmsAdapter;
        this.id = NEXT_ID++;
        this.scale = 0;
        this.trackPos = false;
        this.locked = true;
        this.icons = new ArrayList<>();
        this.data = drawAdapter;
        this.observers = new CopyOnWriteArrayList<>();
    }

    public void send() {
        this.cacheData();
        this.sendCached();
    }

    public void sendLatestSlice(final Player player) {
        this.sendLatestSlice(player, null);
    }

    public void sendLatestSlice(final Player player, final Integer frameId) {
        if (frameId != null) {
            this.nmsAdapter.sendPacket(player, this.nmsAdapter.constructFramePacket(frameId, this.id));
        }
        this.nmsAdapter.sendPacket(player, this.nmsAdapter.constructMapPacket(this.id,
                this.scale,
                this.trackPos,
                this.locked,
                this.icons,
                this.latestSlice.data,
                this.latestSlice.x,
                this.latestSlice.z,
                Math.min(this.latestSlice.w, 128),
                Math.min(this.latestSlice.h, 128)));
    }

    public void sendSlice(final int fromX, final int fromZ, final int width, final int height, final byte[] slice) {
        this.latestSlice = new SliceData() {
            {
                this.data = slice;
                this.x = fromX;
                this.z = fromZ;
                this.w = width;
                this.h = height;
            }
        };
        this.getObservers().forEach(player -> this.sendLatestSlice(player, null));
    }

    public void sendCached() {
        final Object packet = this.nmsAdapter.constructMapPacket(this.id,
                this.scale,
                this.trackPos,
                this.locked,
                this.icons,
                this.cachedData,
                0,
                0,
                128,
                128);
        this.getObservers().forEach(player -> this.nmsAdapter.sendPacket(player, packet));
    }

    public void sendAndSetChanges(final DrawAdapter newData) {
        final DrawAdapter.DiffResult diffResult = this.data.diff(newData);
        final Object packet = this.nmsAdapter.constructMapPacket(this.id,
                this.scale,
                this.trackPos,
                this.locked,
                this.icons,
                /*diffResult.getData() The packet needs the whole data unfortunately...*/ newData.toOneDimArray(),
                diffResult.getMinX(),
                diffResult.getMinZ(),
                diffResult.getWidth(),
                diffResult.getHeight());
        this.getObservers().forEach(player -> this.nmsAdapter.sendPacket(player, packet));
        this.setData(newData);
    }

    public void cacheData() {
        this.cachedData = this.data.toOneDimArray();
    }

    public void addObserver(final Player player) {
        if (this.observers.stream().anyMatch(playerWeakReference -> playerWeakReference.get() == player)) {
            return;
        }
        this.observers.add(new WeakReference<>(player));
    }

    public void removeObserver(final Player player) {
        this.observers.stream()
                .filter(ref -> ref.get() != null && ref.get().getUniqueId().equals(player.getUniqueId()))
                .findAny()
                .ifPresent(ref -> this.observers.remove(ref));
    }

    public void clearObservers() {
        this.observers = this.observers.stream()
                .filter(ref -> ref.get() != null)
                .collect(Collectors.toList());
    }

    public int getId() {
        return this.id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public byte getScale() {
        return this.scale;
    }

    public boolean isTrackPos() {
        return this.trackPos;
    }

    public void setTrackPos(final boolean trackPos) {
        this.trackPos = trackPos;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(final boolean locked) {
        this.locked = locked;
    }

    public List<MapIcon> getIcons() {
        return this.icons;
    }

    public DrawAdapter getData() {
        return this.data;
    }

    public void setData(final DrawAdapter data) {
        this.data = data;
    }

    public List<Player> getObservers() {
        this.clearObservers();
        return this.observers.stream()
                .map(Reference::get)
                .collect(Collectors.toList());
    }

    public void setCachedData(final byte[] cachedData) {
        this.cachedData = cachedData;
    }

    public static class SliceData {

        public byte[] data;
        public int x;
        public int z;
        public int w;
        public int h;
    }

}
