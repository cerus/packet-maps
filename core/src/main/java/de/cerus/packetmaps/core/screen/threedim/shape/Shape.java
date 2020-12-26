package de.cerus.packetmaps.core.screen.threedim.shape;

import de.cerus.packetmaps.core.screen.threedim.Vertex;
import java.util.List;

public abstract class Shape {

    public abstract List<Vertex> getVertices();

    public abstract int getVerticesCount();

}
