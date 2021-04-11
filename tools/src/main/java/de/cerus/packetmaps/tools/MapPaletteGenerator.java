package de.cerus.packetmaps.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

public class MapPaletteGenerator {

    private static final StringBuilder classBuilder = new StringBuilder("public enum MapPalette {\n\n");

    public static void main(final String[] args) throws IOException {
        final File file = new File("map-palette.txt");
        if (!file.exists()) {
            throw new IllegalStateException();
        }

        int partId = 0;
        int colorId = 0;

        final String input = String.join("\n", Files.readAllLines(file.toPath()));
        for (final String part : input.split("</td></tr>")) {
            final String[] ogSplit = part.trim().split("\n");
            final String[] split = Arrays.copyOfRange(ogSplit, 2, 5);
            if (ogSplit.length == 2) {
                partId++;
                colorId = 0;
                continue;
            }

            final int id = Integer.parseInt(split[0].substring(4, split[0].length() - 5));
            final int[] rgb = (id <= 3) ? (new int[0]) : Arrays.stream(split[1].substring("<td style=\"background-color: rgb(".length(),
                    split[1].length() - ")\"></td>".length()).split(","))
                    .map(String::trim)
                    .mapToInt(Integer::parseInt)
                    .toArray();
            System.out.println("PART: " + partId + " COLOR: " + colorId + " ID: " + id + " RGB: " + Arrays.toString(rgb));

            write(partId, colorId, id, rgb);

            colorId++;
        }

        for (int i = 0; i < 3; i++) {
            classBuilder.deleteCharAt(classBuilder.length() - 1);
        }
        classBuilder.append(";\n\n    private final int id;\n" +
                "    private final Color color;\n" +
                "\n" +
                "    MapPalette(final int id, final Color color) {\n" +
                "        this.id = id;\n" +
                "        this.color = color;\n" +
                "    }\n" +
                "\n" +
                "    public int getId() {\n" +
                "        return this.id;\n" +
                "    }\n" +
                "\n" +
                "    public Color getColor() {\n" +
                "        return color;\n" +
                "    }\n\n}");

        final File outFile = new File("MapPalette.java.txt");
        outFile.createNewFile();

        try (final FileOutputStream outputStream = new FileOutputStream(outFile)) {
            outputStream.write(classBuilder.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    private static void write(final int partId, final int colorId, final int id, final int[] rgb) {
        classBuilder.append("    PART_").append(partId).append("_").append(colorId).append("(").append(id).append(", ")
                .append(rgb.length != 3 ? "null" : String.format("new Color(%d, %d, %d)", rgb[0], rgb[1], rgb[2])).append("),\n");

        if (colorId == 3) {
            classBuilder.append("\n");
        }
    }

}
