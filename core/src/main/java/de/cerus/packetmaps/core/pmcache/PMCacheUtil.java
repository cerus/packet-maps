package de.cerus.packetmaps.core.pmcache;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class PMCacheUtil {

    private PMCacheUtil() {
    }

    public static <T extends Cache> T read(final File file) throws IOException {
        if (!file.exists()) {
            return null;
        }

        return read(new FileInputStream(file));
    }

    public static <T extends Cache> T read(final URL url) throws IOException {
        final URLConnection urlConnection = url.openConnection();
        urlConnection.setDoInput(true);

        return read(urlConnection.getInputStream());
    }

    public static <T extends Cache> T read(final InputStream inputStream) throws IOException {
        final DataInputStream dataInputStream = new DataInputStream(inputStream);
        final int ver = dataInputStream.readInt();

        switch (ver) {
            case 0:
                return readV0(dataInputStream);
            default:
                throw new IllegalStateException("Unknown version");
        }
    }

    public static void write(final String path, final String name, final Cache cache) throws IOException {
        final FileOutputStream outputStream = new FileOutputStream(new File(path, name + ".pmcache"));
        write(outputStream, cache);
        outputStream.close();
    }

    public static void write(final File file, final Cache cache) throws IOException {
        if (!file.exists()) {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
        }

        final FileOutputStream outputStream = new FileOutputStream(file);
        write(outputStream, cache);
        outputStream.close();
    }

    public static void write(final OutputStream outputStream, final Cache cache) throws IOException {
        final DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        writeV0(dataOutputStream, cache);
        dataOutputStream.close();
    }

    private static <T extends Cache> T readV0(final DataInputStream dataInputStream) throws IOException {
        final int type = dataInputStream.readInt();

        final Cache cache;
        switch (type) {
            case FramedCache.ID:
                cache = new FramedCache();
                break;
            default:
                throw new IllegalStateException("Unknown type");
        }

        cache.read(dataInputStream);
        return (T) cache;
    }

    private static void writeV0(final DataOutputStream dataOutputStream, final Cache cache) throws IOException {
        dataOutputStream.writeInt(0); // Write version
        cache.write(dataOutputStream);
    }

}
