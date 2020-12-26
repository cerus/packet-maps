package de.cerus.packetmaps.core.screen.threedim.shape;

import de.cerus.packetmaps.core.screen.threedim.Vertex;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;

public class Triangle extends Shape implements Colored {

    private final Vertex v1;
    private final Vertex v2;
    private final Vertex v3;
    private final Color color;

    public Triangle(final Vertex v1, final Vertex v2, final Vertex v3, final Color color) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.color = color;
    }

    public Vertex getV1() {
        return this.v1;
    }

    public Vertex getV2() {
        return this.v2;
    }

    public Vertex getV3() {
        return this.v3;
    }

    @Override
    public Color getColor() {
        return this.color;
    }

    @Override
    public List<Vertex> getVertices() {
        return Arrays.asList(this.v1, this.v2, this.v3);
    }

    @Override
    public int getVerticesCount() {
        return 3;
    }

}
