package de.cerus.packetmaps.core.pmcache;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Cache {

    void write(DataOutputStream outputStream) throws IOException;

    void read(DataInputStream inputStream) throws IOException;

    int getId();

}
