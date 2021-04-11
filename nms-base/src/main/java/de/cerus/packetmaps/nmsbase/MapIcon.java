package de.cerus.packetmaps.nmsbase;

public class MapIcon {

    private final int x;
    private final int y;
    private final int rotation;
    private final String name;
    private final Type type;

    public MapIcon(final Type type, final int x, final int y, final int rotation, final String name) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.rotation = rotation;
        this.name = name;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getRotation() {
        return this.rotation;
    }

    public String getName() {
        return this.name;
    }

    public Type getType() {
        return this.type;
    }

    public enum Type {
        PLAYER,
        FRAME,
        RED_MARKER,
        BLUE_MARKER,
        TARGET_X,
        TARGET_POINT,
        PLAYER_OFF_MAP,
        PLAYER_OFF_LIMITS,
        MANSION,
        MONUMENT,
        BANNER_WHITE,
        BANNER_ORANGE,
        BANNER_MAGENTA,
        BANNER_LIGHT_BLUE,
        BANNER_YELLOW,
        BANNER_LIME,
        BANNER_PINK,
        BANNER_GRAY,
        BANNER_LIGHT_GRAY,
        BANNER_CYAN,
        BANNER_PURPLE,
        BANNER_BLUE,
        BANNER_BROWN,
        BANNER_GREEN,
        BANNER_RED,
        BANNER_BLACK,
        RED_X
    }
}
