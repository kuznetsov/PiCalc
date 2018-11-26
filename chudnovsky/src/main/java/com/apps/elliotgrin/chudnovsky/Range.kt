package com.apps.elliotgrin.chudnovsky

class Range(val initialK: Long, val finalK: Long) {

    init {
        if (initialK < 0 || finalK < 0) {
            throw IllegalArgumentException("Bounds should be strictly positive.")
        }

        if (finalK < initialK) {
            throw IllegalArgumentException("Upper bound should be greater than lower bound.")
        }
    }

    override fun toString(): String {
        return initialK.toString() + " " + finalK
    }
}
