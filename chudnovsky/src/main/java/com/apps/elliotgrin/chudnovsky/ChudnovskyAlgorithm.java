package com.apps.elliotgrin.chudnovsky;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;
import org.apfloat.ApintMath;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ChudnovskyAlgorithm {

    /**
     * Finds the mathematical constant pi to <code>precision</code> number of digits. Single-threaded.
     *
     * @param precision desired for return value
     * @return mathematical constant pi
     * @see <a href="http://www.craig-wood.com/nick/articles/pi-chudnovsky/">http://www.craig-wood.com/nick/articles/pi-chudnovsky/</a> for details
     */
    public static Apfloat calculatePi(long precision) {
        // need one extra place for the 3, and one extra place for some rounding issues
        precision = precision + 2;
        Apfloat negativeOne = new Apfloat(-1l);

        Apfloat two = new Apfloat(2l);
        Apfloat five = new Apfloat(5l);
        Apfloat six = new Apfloat(6l);
        Apfloat C = new Apfloat(640320l);
        Apfloat C3_OVER_24 = (C.multiply(C).multiply(C)).divide(new Apfloat(24, precision));
        Apfloat DIGITS_PER_TERM = ApfloatMath.log(C3_OVER_24.divide(new Apfloat(72, precision)), new Apfloat(10l));

        // find the first term in the series
        Apfloat k = new Apfloat(0l);
        Apfloat a_k = new Apfloat(1l, precision);

        Apfloat a_sum = new Apfloat(1l);
        Apfloat b_sum = new Apfloat(0l);
        k = k.add(Apfloat.ONE);

        long numberOfLoopsToRun = new Apfloat(precision, precision).divide(DIGITS_PER_TERM).add(Apfloat.ONE).longValue();

        while (k.longValue() < numberOfLoopsToRun) {
            a_k = a_k.multiply(negativeOne.multiply((six.multiply(k).subtract(five)).multiply(two.multiply(k).subtract(Apfloat.ONE)).multiply(six.multiply(k).subtract(Apfloat.ONE))));
            a_k = a_k.divide(k.multiply(k).multiply(k).multiply(C3_OVER_24));
            a_sum = a_sum.add(a_k);
            b_sum = b_sum.add(k.multiply(a_k));
            k = k.add(Apfloat.ONE);
        }

        Apfloat total = new Apfloat(13591409l).multiply(a_sum).add(new Apfloat(545140134l).multiply(b_sum));

        Apfloat sqrtTenThousandAndFive = ApfloatMath.sqrt(new Apfloat(10005l, precision));
        Apfloat pi = (new Apfloat(426880l).multiply(sqrtTenThousandAndFive).divide(total)).precision(precision - 1);

        return pi;
    }

    /**
     * Finds the mathematical constant pi to <code>precision</code> number of digits. Multi-threaded.
     *
     * @param precision       desired for return value
     * @param numberOfThreads to run in parallel
     * @return mathematical constant pi
     */
    public static Apfloat calculatePi(final long precision, int numberOfThreads) {
        List<Range> ranges = ChudnovskyAlgorithm.calculateTermRanges(numberOfThreads, precision);

        ExecutorService executor = Executors.newFixedThreadPool(ranges.size());
        List<Future<Pair<Apfloat, Apfloat>>> futures = new ArrayList<>(ranges.size());
        for (final Range r : ranges) {
            futures.add(executor.submit(new Callable<Pair<Apfloat, Apfloat>>() {
                @Override
                public Pair<Apfloat, Apfloat> call() throws Exception {
                    return ChudnovskyAlgorithm.calculateTermSums(r, precision);
                }
            }));
        }
        executor.shutdown();
        try {
            executor.awaitTermination(30l, TimeUnit.MINUTES);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
            return null;
        }

        List<Pair<Apfloat, Apfloat>> termSums = new ArrayList<>(futures.size());

        for (Future<Pair<Apfloat, Apfloat>> f : futures) {
            try {
                termSums.add(f.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return ChudnovskyAlgorithm.merge(termSums, precision);
    }

    /**
     * Figures out which ranges {@link ChudnovskyAlgorithm#calculateTermSums(Range, long)} should act on.
     *
     * @param numberOfRanges How many ranges you want returned
     * @param precision
     * @return mutually exclusive ranges that can run executed in parallel.
     */
    public static List<Range> calculateTermRanges(long numberOfRanges, long precision) {
        if (numberOfRanges <= 0) {
            throw new IllegalArgumentException("Number of ranges should be positive.");
        }

        List<Range> ranges = new ArrayList<Range>();

        Apfloat C = new Apfloat(640320l);
        Apfloat C3_OVER_24 = (C.multiply(C).multiply(C)).divide(new Apfloat(24, precision));
        Apfloat DIGITS_PER_TERM = ApfloatMath.log(C3_OVER_24.divide(new Apfloat(72, precision)), new Apfloat(10l));

        Long numberOfTerms = (new Apfloat(precision, precision)).divide(DIGITS_PER_TERM).ceil().longValue();

        double rangeSize = numberOfTerms.doubleValue() / Long.valueOf(numberOfRanges).doubleValue();

        for (double i = 0.0; i < numberOfTerms; i += rangeSize) {
            double f = (i + rangeSize);

            long il = (long) i;
            long fl = (long) f;

            il = Math.min(il, numberOfTerms);
            fl = Math.min(fl, numberOfTerms);
            ranges.add(new Range(il, fl));
        }
        return ranges;
    }

    /**
     * Method to be run in parallel.
     *
     * @param range     from {@link ChudnovskyAlgorithm#calculateTermRanges(long, long)}
     * @param precision desired for return value
     * @return Pair contains a_sum and b_sum.  To be feed into {@link ChudnovskyAlgorithm#merge(List, long)} ;
     */
    public static Pair<Apfloat, Apfloat> calculateTermSums(Range range, long precision) {
        // need one extra place for the 3, and one extra place for some rounding issues
        precision = precision + 2;
        Apfloat negativeOne = new Apfloat(-1l);

        Apfloat two = new Apfloat(2l);
        Apfloat three = new Apfloat(3l);
        Apfloat five = new Apfloat(5l);
        Apfloat six = new Apfloat(6l);
        Apfloat C = new Apfloat(640320l);
        Apfloat C3_OVER_24 = (C.multiply(C).multiply(C)).divide(new Apfloat(24, precision));
        //Apfloat DIGITS_PER_TERM = ApfloatMath.log(C3_OVER_24.divide(new Apfloat(72, precision)), new Apfloat(10l));

        // find the first term in the series
        Apfloat k = new Apfloat(range.initalK);
        // NOTE: need to push out the precision in this term by a bit for the division to work properly.  8% is probably too high, but should be a safe estimate
        Apfloat a_k = ((k.longValue() % 2 == 0) ? Apfloat.ONE : negativeOne).multiply(ApintMath.factorial(6 * k.longValue())).precision((long) (precision * 1.08));
        Apfloat kFactorial = ApintMath.factorial(k.longValue());
        a_k = a_k.divide(ApintMath.factorial(three.multiply(k).longValue()).multiply(kFactorial.multiply(kFactorial).multiply(kFactorial)).multiply(ApfloatMath.pow(C, k.longValue() * 3)));

        Apfloat a_sum = new Apfloat(0l).add(a_k);
        Apfloat b_sum = new Apfloat(0l).add(k.multiply(a_k));
        k = k.add(Apfloat.ONE);

        for (long i = range.initalK + 1; i < range.finalK; i++) {
            a_k = a_k.multiply(negativeOne.multiply((six.multiply(k).subtract(five)).multiply(two.multiply(k).subtract(Apfloat.ONE)).multiply(six.multiply(k).subtract(Apfloat.ONE))));
            a_k = a_k.divide(k.multiply(k).multiply(k).multiply(C3_OVER_24));
            a_sum = a_sum.add(a_k);
            b_sum = b_sum.add(k.multiply(a_k));
            k = k.add(Apfloat.ONE);
        }

        if (range.initalK == range.finalK) {
            a_sum = new Apfloat(0l);
            b_sum = new Apfloat(0l);
        }

        return new ImmutablePair<>(a_sum, b_sum);
    }

    /**
     * Merges the results from {@link ChudnovskyAlgorithm#calculateTermSums(Range, long)}.
     *
     * @param termSums  list of the pairs of a_ums and b_sums to merge
     * @param precision desired for return value
     * @return mathematical constant pi
     */
    public static Apfloat merge(List<Pair<Apfloat, Apfloat>> termSums, long precision) {
        Apfloat a_sum = new Apfloat(0l);
        Apfloat b_sum = new Apfloat(0l);

        for (Pair<Apfloat, Apfloat> termSum : termSums) {
            a_sum = a_sum.add(termSum.getLeft());
            b_sum = b_sum.add(termSum.getRight());
        }

        precision++;
        Apfloat total = new Apfloat(13591409l).multiply(a_sum).add(new Apfloat(545140134l).multiply(b_sum));

        Apfloat sqrtTenThousandAndFive = ApfloatMath.sqrt(new Apfloat(10005l, precision + 1));
        Apfloat pi = (new Apfloat(426880l).multiply(sqrtTenThousandAndFive).divide(total)).precision(precision);

        return pi;
    }
}

