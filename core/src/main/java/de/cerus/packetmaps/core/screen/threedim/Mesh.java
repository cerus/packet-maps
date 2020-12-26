package de.cerus.packetmaps.core.screen.threedim;

public class Mesh {

    String name;
    Vertex[] vertices;
    Vertex position;
    Vertex rotation;

    public Mesh(final String name, final Vertex[] vertices, final Vertex position, final Vertex rotation) {
        this.name = name;
        this.vertices = vertices;
        this.position = position;
        this.rotation = rotation;
    }

    public String getName() {
        return this.name;
    }

    public Vertex getPosition() {
        return this.position;
    }

    public Vertex getRotation() {
        return this.rotation;
    }

    public Vertex[] getVertices() {
        return this.vertices;
    }

}
