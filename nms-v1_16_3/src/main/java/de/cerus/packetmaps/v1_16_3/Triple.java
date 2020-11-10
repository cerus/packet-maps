package de.cerus.packetmaps.v1_16_3;

public class Triple<F, S, T> {

    private final F first;
    private final S second;
    private final T third;

    public Triple(final F first, final S second, final T third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public F getFirst() {
        return this.first;
    }

    public S getSecond() {
        return this.second;
    }

    public T getThird() {
        return this.third;
    }

}
