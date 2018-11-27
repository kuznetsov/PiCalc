package com.apps.elliotgrin.chudnovsky

import org.junit.Assert.assertEquals
import org.junit.Test

class MemoizationTest {

    private val alg = ChudnovskyAlgorithm()

    @Test
    fun testMemoization() {
        for (i in 1..1000L) {
            val piWithoutMem = alg.calculatePi(i)
            val piWithMem = alg.calculatePiWithMemoization(i)
            println("$i : $piWithMem")
            assertEquals(piWithoutMem, piWithMem)
        }
    }

}