package com.apps.elliotgrin.chudnovsky

import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.Pair
import org.apfloat.Apfloat
import org.apfloat.ApfloatMath
import org.apfloat.ApintMath

import java.util.ArrayList
import java.util.concurrent.*

private val NEG_ONE = Apfloat(-1L)
private val TWO = Apfloat(2L)
private val THREE = Apfloat(3L)
private val FIVE = Apfloat(5L)
private val SIX = Apfloat(6L)
private val C = Apfloat(640320L)

class ChudnovskyAlgorithm {

    // Cache structure used in memoization
    val cache = mutableMapOf<TripleFloat, Apfloat>()

    // Clear cache data
    fun clearCache() = cache.clear()


    // region calculation

    /**
     * Finds the mathematical constant pi to `precision` number of digits. Single-threaded.
     */
    fun calculatePi(prec: Long, withMem: Boolean): Apfloat {
        // need one extra place for the 3, and one extra place for some rounding issues
        val precision = prec + 2

        val c3Over24 = C.multiply(C).multiply(C).divide(Apfloat(24, precision))
        val digitsPerTerm = ApfloatMath.log(c3Over24.divide(Apfloat(72, precision)), Apfloat(10L))

        // find the first term in the series
        var k = Apfloat(0L)
        var aK = Apfloat(1L, precision)

        var aSum = Apfloat(1L)
        var bSum = Apfloat(0L)
        k = k.add(Apfloat.ONE)

        val numberOfLoopsToRun = Apfloat(precision, precision).divide(digitsPerTerm).add(Apfloat.ONE).toLong()

        while (k.toLong() < numberOfLoopsToRun) {
            aK = if (withMem) {
                val key = TripleFloat(aK, k, c3Over24)
                if (cache.containsKey(key)) {
                    cache[key]!!
                } else {
                    cache[key] = calculateAk(aK, k, c3Over24)
                    cache[key]!!
                }
            } else {
                calculateAk(aK, k, c3Over24)
            }
            aSum = aSum.add(aK)
            bSum = bSum.add(k.multiply(aK))
            k = k.add(Apfloat.ONE)
        }

        val total = Apfloat(13591409L).multiply(aSum).add(Apfloat(545140134L).multiply(bSum))

        val sqrtTenThousandAndFive = ApfloatMath.sqrt(Apfloat(10005L, precision))

        return Apfloat(426880L).multiply(sqrtTenThousandAndFive).divide(total).precision(precision - 1)
    }

    /*fun calculatePiWithMemoization(prec: Long): Apfloat {
        // need one extra place for the 3, and one extra place for some rounding issues
        val precision = prec + 2

        val c3Over24 = C.multiply(C).multiply(C).divide(Apfloat(24, precision))
        val digitsPerTerm = ApfloatMath.log(c3Over24.divide(Apfloat(72, precision)), Apfloat(10L))

        // find the first term in the series
        var k = Apfloat(0L)
        var aK = Apfloat(1L, precision)

        var aSum = Apfloat(1L)
        var bSum = Apfloat(0L)
        k = k.add(Apfloat.ONE)

        val numberOfLoopsToRun = Apfloat(precision, precision).divide(digitsPerTerm).add(Apfloat.ONE).toLong()

        while (k.toLong() < numberOfLoopsToRun) {
            val key = TripleFloat(aK, k, c3Over24)
            aK = if (cache.containsKey(key)) {
                cache[key]!!
            } else {
                cache[key] = calculateAk(aK, k, c3Over24)
                cache[key]!!
            }

            aSum = aSum.add(aK)
            bSum = bSum.add(k.multiply(aK))
            k = k.add(Apfloat.ONE)
        }

        val total = Apfloat(13591409L).multiply(aSum).add(Apfloat(545140134L).multiply(bSum))

        val sqrtTenThousandAndFive = ApfloatMath.sqrt(Apfloat(10005L, precision))

        return Apfloat(426880L).multiply(sqrtTenThousandAndFive).divide(total).precision(precision - 1)
    }*/

    /**
     * Finds the mathematical constant pi to `precision` number of digits. Multi-threaded.
     */
    fun calculatePi(precision: Long, withMem: Boolean, numberOfThreads: Int): Apfloat? {
        val ranges = calculateTermRanges(numberOfThreads.toLong(), precision)

        val executor = Executors.newFixedThreadPool(ranges.size)
        val futures = ArrayList<Future<Pair<Apfloat, Apfloat>>>(ranges.size)
        for (r in ranges) {
            futures.add(executor.submit(Callable { calculateTermSums(r, precision, withMem) }))
        }
        executor.shutdown()
        try {
            executor.awaitTermination(30L, TimeUnit.MINUTES)
        } catch (ie: InterruptedException) {
            ie.printStackTrace()
            return null
        }

        val termSums = ArrayList<Pair<Apfloat, Apfloat>>(futures.size)

        for (f in futures) {
            try {
                termSums.add(f.get())
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        return merge(termSums, precision)
    }

    // endregion


    // region util

    /**
     * Used in general calculation
     */
    private fun calculateAk(aK: Apfloat, k: Apfloat, c: Apfloat): Apfloat =
        aK.multiply(
            NEG_ONE.multiply(
                SIX.multiply(k).subtract(FIVE).multiply(
                    TWO.multiply(k).subtract(Apfloat.ONE)
                ).multiply(SIX.multiply(k).subtract(Apfloat.ONE))
            )
        ).divide(k.multiply(k).multiply(k).multiply(c))

    /**
     * Method to be run in parallel.
     */
    private fun calculateTermSums(range: Range, prec: Long, withMem: Boolean): Pair<Apfloat, Apfloat> {
        // need one extra place for the 3, and one extra place for some rounding issues
        val precision = prec + 2

        val c3Over24 = C.multiply(C).multiply(C).divide(Apfloat(24, precision))

        // find the first term in the series
        var k = Apfloat(range.initialK)
        // NOTE: need to push out the precision in this term by a bit for the division to work properly.  8% is probably too high, but should be a safe estimate
        var aK = (if (k.toLong() % 2 == 0L) Apfloat.ONE else NEG_ONE).multiply(ApintMath.factorial(6 * k.toLong()))
            .precision((precision * 1.08).toLong())
        val kFactorial = ApintMath.factorial(k.toLong())
        aK = aK.divide(
            ApintMath.factorial(THREE.multiply(k).toLong()).multiply(
                kFactorial.multiply(kFactorial).multiply(kFactorial)
            ).multiply(ApfloatMath.pow(C, k.toLong() * 3))
        )

        var aSum = Apfloat(0L).add(aK)
        var bSum = Apfloat(0L).add(k.multiply(aK))
        k = k.add(Apfloat.ONE)

        for (i in range.initialK + 1 until range.finalK) {
            aK = if (withMem) {
                val key = TripleFloat(aK, k, c3Over24)
                if (cache.containsKey(key)) {
                    cache[key]!!
                } else {
                    cache[key] = calculateAk(aK, k, c3Over24)
                    cache[key]!!
                }
            } else {
                calculateAk(aK, k, c3Over24)
            }

            aSum = aSum.add(aK)
            bSum = bSum.add(k.multiply(aK))
            k = k.add(Apfloat.ONE)
        }

        if (range.initialK == range.finalK) {
            aSum = Apfloat(0L)
            bSum = Apfloat(0L)
        }

        return ImmutablePair(aSum, bSum)
    }

    /**
     * Figures out which ranges calculateTermSums should act on.
     */
    private fun calculateTermRanges(numberOfRanges: Long, precision: Long): List<Range> {
        if (numberOfRanges <= 0) {
            throw IllegalArgumentException("Number of ranges should be positive.")
        }

        val ranges = ArrayList<Range>()

        val C = Apfloat(640320L)
        val C3_OVER_24 = C.multiply(C).multiply(C).divide(Apfloat(24, precision))
        val DIGITS_PER_TERM = ApfloatMath.log(C3_OVER_24.divide(Apfloat(72, precision)), Apfloat(10L))

        val numberOfTerms = Apfloat(precision, precision).divide(DIGITS_PER_TERM).ceil().toLong()

        val rangeSize = numberOfTerms.toDouble() / java.lang.Long.valueOf(numberOfRanges).toDouble()

        var i = 0.0
        while (i < numberOfTerms) {
            val f = i + rangeSize

            var il = i.toLong()
            var fl = f.toLong()

            il = Math.min(il, numberOfTerms)
            fl = Math.min(fl, numberOfTerms)
            ranges.add(Range(il, fl))
            i += rangeSize
        }
        return ranges
    }

    /**
     * Merges the results from calculateTermSums.
     */
    private fun merge(termSums: List<Pair<Apfloat, Apfloat>>, prec: Long): Apfloat {
        var precision = prec
        var aSum = Apfloat(0L)
        var bSum = Apfloat(0L)

        for (termSum in termSums) {
            aSum = aSum.add(termSum.left)
            bSum = bSum.add(termSum.right)
        }

        precision++
        val total = Apfloat(13591409L).multiply(aSum).add(Apfloat(545140134L).multiply(bSum))

        val sqrtTenThousandAndFive = ApfloatMath.sqrt(Apfloat(10005L, precision + 1))

        return Apfloat(426880L).multiply(sqrtTenThousandAndFive).divide(total).precision(precision)
    }

    // endregion
}

data class TripleFloat(val a: Apfloat, val b: Apfloat, val c: Apfloat) {

    override fun hashCode() = a.hashCode() + b.hashCode() + c.hashCode()

    override fun equals(other: Any?): Boolean =
        if (other is TripleFloat) {
            a == other.a && a.precision() == other.a.precision() &&
                    b == other.b && b.precision() == other.b.precision() &&
                    c == other.c && c.precision() == other.c.precision()
        } else {
            false
        }
}
