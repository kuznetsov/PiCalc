package com.apps.elliotgrin.chudnovsky

import java.util.Date
import java.util.Scanner

fun main(args: Array<String>) {
    println("Enter k:")
    val scanner = Scanner(System.`in`)
    val k = scanner.nextLong()

    // Single threaded test

    var start = Date()
    var pi = calculatePi(k).toString(true)
    var time: Double? = (Date().time - start.time) / 1000.0
    println("Single threaded test:")
    println("Pi = " + pi + ";\nTime = " + time!!.toString() + " s.")
    println()

    // Multithreaded test

    start = Date()
    pi = calculatePi(k, 4)!!.toString(true)
    time = (Date().time - start.time) / 1000.0
    println("Multithreaded test")
    println("Pi = " + pi + ";\nTime = " + time.toString() + " s.")
}
