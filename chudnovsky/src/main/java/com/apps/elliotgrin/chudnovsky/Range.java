package com.apps.elliotgrin.chudnovsky;

public class Range {
    public long initalK;
    public long finalK;

    public Range(long initalK, long finalK) {
        if (initalK < 0 || finalK < 0) {
            throw new IllegalArgumentException("Bounds should be strictly positive.");
        }

        if (finalK < initalK) {
            throw new IllegalArgumentException("Upper bound should be greater than lower bound.");
        }

        this.initalK = initalK;
        this.finalK = finalK;
    }

    @Override
    public String toString() {
        return initalK + " " + finalK;
    }
}
