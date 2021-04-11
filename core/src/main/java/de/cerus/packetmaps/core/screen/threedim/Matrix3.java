package de.cerus.packetmaps.core.screen.threedim;

public class Matrix3 {

    private final double[] values;

    public Matrix3(final double[] values) {
        this.values = values;
    }

    public static Matrix3 rotateXY(final double heading) {
        return new Matrix3(new double[] {
                Math.cos(heading), Math.sin(heading), 0,
                -Math.sin(heading), Math.cos(heading), 0,
                0, 0, 1
        });
    }

    public static Matrix3 rotateYZ(final double heading) {
        return new Matrix3(new double[] {
                1, 0, 0,
                0, Math.cos(heading), Math.sin(heading),
                0, -Math.sin(heading), Math.cos(heading)
        });
    }

    public static Matrix3 rotateXZ(final double heading) {
        return new Matrix3(new double[] {
                Math.cos(heading), 0, -Math.sin(heading),
                0, 1, 0,
                Math.sin(heading), 0, Math.cos(heading)
        });
    }

    public Matrix3 multiply(final Matrix3 other) {
        final double[] result = new double[9];
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                for (int i = 0; i < 3; i++) {
                    result[row * 3 + col] += this.values[row * 3 + i] * other.values[i * 3 + col];
                }
            }
        }
        return new Matrix3(result);
    }

    public Vertex transform(final Vertex in) {
        return new Vertex(
                in.x * this.values[0] + in.y * this.values[3] + in.z * this.values[6],
                in.x * this.values[1] + in.y * this.values[4] + in.z * this.values[7],
                in.x * this.values[2] + in.y * this.values[5] + in.z * this.values[8]
        );
    }

    public double[] getValues() {
        return this.values;
    }

}