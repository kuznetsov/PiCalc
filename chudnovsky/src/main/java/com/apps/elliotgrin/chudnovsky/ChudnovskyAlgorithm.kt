package com.apps.elliotgrin.chudnovsky

import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.Pair
import org.apfloat.Apfloat
import org.apfloat.ApfloatMath
import org.apfloat.ApintMath

import java.util.ArrayList
import java.util.concurrent.*

/**
 * Finds the mathematical constant pi to `precision` number of digits. Single-threaded.
 *
 * @param precision desired for return value
 * @return mathematical constant pi
 * @see [http://www.craig-wood.com/nick/articles/pi-chudnovsky/](http://www.craig-wood.com/nick/articles/pi-chudnovsky/) for details
 */
fun calculatePi(prec: Long): Apfloat {
    var precision = prec
    // need one extra place for the 3, and one extra place for some rounding issues
    precision += 2
    val negativeOne = Apfloat(-1L)

    val two = Apfloat(2L)
    val five = Apfloat(5L)
    val six = Apfloat(6L)
    val C = Apfloat(640320L)
    val C3_OVER_24 = C.multiply(C).multiply(C).divide(Apfloat(24, precision))
    val DIGITS_PER_TERM = ApfloatMath.log(C3_OVER_24.divide(Apfloat(72, precision)), Apfloat(10L))

    // find the first term in the series
    var k = Apfloat(0L)
    var aK = Apfloat(1L, precision)

    var aSum = Apfloat(1L)
    var bSum = Apfloat(0L)
    k = k.add(Apfloat.ONE)

    val numberOfLoopsToRun = Apfloat(precision, precision).divide(DIGITS_PER_TERM).add(Apfloat.ONE).toLong()

    while (k.toLong() < numberOfLoopsToRun) {
        aK = aK.multiply(
            negativeOne.multiply(
                six.multiply(k).subtract(five).multiply(
                    two.multiply(k).subtract(Apfloat.ONE)
                ).multiply(six.multiply(k).subtract(Apfloat.ONE))
            )
        )
        aK = aK.divide(k.multiply(k).multiply(k).multiply(C3_OVER_24))
        aSum = aSum.add(aK)
        bSum = bSum.add(k.multiply(aK))
        k = k.add(Apfloat.ONE)
    }

    val total = Apfloat(13591409L).multiply(aSum).add(Apfloat(545140134L).multiply(bSum))

    val sqrtTenThousandAndFive = ApfloatMath.sqrt(Apfloat(10005L, precision))

    return Apfloat(426880L).multiply(sqrtTenThousandAndFive).divide(total).precision(precision - 1)
}

/**
 * Finds the mathematical constant pi to `precision` number of digits. Multi-threaded.
 *
 * @param precision       desired for return value>
 * @param numberOfThreads to run in parallel
 * @return mathematical constant pi
 */
fun calculatePi(precision: Long, numberOfThreads: Int): Apfloat? {
    val ranges = calculateTermRanges(numberOfThreads.toLong(), precision)

    val executor = Executors.newFixedThreadPool(ranges.size)
    val futures = ArrayList<Future<Pair<Apfloat, Apfloat>>>(ranges.size)
    for (r in ranges) {
        futures.add(executor.submit(Callable { calculateTermSums(r, precision) }))
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

/**
 * Figures out which ranges [ChudnovskyAlgorithm.calculateTermSums] should act on.
 *
 * @param numberOfRanges How many ranges you want returned
 * @param precision
 * @return mutually exclusive ranges that can run executed in parallel.
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
 * Method to be run in parallel.
 *
 * @param range     from [ChudnovskyAlgorithm.calculateTermRanges]
 * @param precision desired for return value
 * @return Pair contains a_sum and b_sum.  To be feed into [ChudnovskyAlgorithm.merge] ;
 */
private fun calculateTermSums(range: Range, prec: Long): Pair<Apfloat, Apfloat> {
    var precision = prec
    // need one extra place for the 3, and one extra place for some rounding issues
    precision += 2
    val negativeOne = Apfloat(-1L)

    val two = Apfloat(2L)
    val three = Apfloat(3L)
    val five = Apfloat(5L)
    val six = Apfloat(6L)
    val C = Apfloat(640320L)
    val C3_OVER_24 = C.multiply(C).multiply(C).divide(Apfloat(24, precision))

    // find the first term in the series
    var k = Apfloat(range.initialK)
    // NOTE: need to push out the precision in this term by a bit for the division to work properly.  8% is probably too high, but should be a safe estimate
    var aK = (if (k.toLong() % 2 == 0L) Apfloat.ONE else negativeOne).multiply(ApintMath.factorial(6 * k.toLong()))
        .precision((precision * 1.08).toLong())
    val kFactorial = ApintMath.factorial(k.toLong())
    aK = aK.divide(
        ApintMath.factorial(three.multiply(k).toLong()).multiply(
            kFactorial.multiply(kFactorial).multiply(kFactorial)
        ).multiply(ApfloatMath.pow(C, k.toLong() * 3))
    )

    var aSum = Apfloat(0L).add(aK)
    var bSum = Apfloat(0L).add(k.multiply(aK))
    k = k.add(Apfloat.ONE)

    for (i in range.initialK + 1 until range.finalK) {
        aK = aK.multiply(
            negativeOne.multiply(
                six.multiply(k).subtract(five).multiply(
                    two.multiply(k).subtract(Apfloat.ONE)
                ).multiply(six.multiply(k).subtract(Apfloat.ONE))
            )
        )
        aK = aK.divide(k.multiply(k).multiply(k).multiply(C3_OVER_24))
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
 * Merges the results from [ChudnovskyAlgorithm.calculateTermSums].
 *
 * @param termSums  list of the pairs of a_ums and b_sums to merge
 * @param precision desired for return value
 * @return mathematical constant pi
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

