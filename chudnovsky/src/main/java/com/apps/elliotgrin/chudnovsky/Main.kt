package com.apps.elliotgrin.chudnovsky

import java.util.Date
import java.util.Scanner

fun main(args: Array<String>) {

    println("Enter k:")
    val scanner = Scanner(System.`in`)
    val k = scanner.nextLong()

    testAll(k)
}


private fun testAll(k: Long) {

    // Single threaded:
    println("----- Single threaded test: ----- ")
    testMemoization(k, false)

    // Multithreaded
    println("----- Multithreaded test: ----- ")
    testMemoization(k, true)
}

private fun testMemoization(k: Long, multiThreaded: Boolean) {
    println("Without memoization:")
    var start = Date()
    calcInCycle(k, multiThreaded, false)
    var time = (Date().time - start.time) / 1000.0
    printTime(time)

    if (multiThreaded) return

    println("With memoization:")
    start = Date()
    calcInCycle(k, multiThreaded, true)
    time = (Date().time - start.time) / 1000.0
    printTime(time)
    println()
}

private fun calcInCycle(k: Long, multiThreaded: Boolean, withMem: Boolean) {
    val alg = ChudnovskyAlgorithm()
    for (i in 1..k) {

        when {
            !multiThreaded && !withMem -> alg.calculatePi(k)
            !multiThreaded && withMem -> alg.calculatePiWithMemoization(k)
            multiThreaded -> alg.calculatePi(k, 4)
        }

    }

    alg.clearCache()
}

private fun printTime(time: Double) {
    println("Time = $time sec")
}
