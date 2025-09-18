package com.cdev.mathengine.utils;

import com.cdev.mathengine.api.core.BigNumber;
import com.cdev.mathengine.api.core.Functions;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PiCalc {

    static int ctr = 0;

    public void run() {
        calcPi(100000);
    }

    public void calcPi(int digits) {
        int systemCores = Runtime.getRuntime().availableProcessors() * 2;

        int iterations = (int) Math.ceil(digits / 14d);
        System.out.println("Iterations: " + iterations);
        int precision = (int) (digits * 1.3);
        MathContext mc = new MathContext(precision, RoundingMode.HALF_UP);

        List<BigNumber> termStore = Collections.synchronizedList(new ArrayList<>());
        List<Runnable> runnableStore = new ArrayList<>();

        int iterationsPerThread = (int) Math.ceil((double) iterations / (double) systemCores);

        Object lock = new String("lock");

        boolean run = true;
        for (int start = 0, end = iterationsPerThread; run; start += iterationsPerThread, end += iterationsPerThread) {
            int lStart = start;
            int lEnd = end;
            Runnable runnable = () -> {
                BigNumber sum = BigNumber.of("0", mc);
                for (int i = lStart; i < lEnd; i++) {
                    sum = sum.add(calcTerm(i, mc));
                    synchronized (lock) {
                        System.out.println(++ctr);
                    }
                }
                termStore.add(sum);
                //0-3 4-7 8-11 12-15 16-19 20-23 24-27 28-31 32-35 36-39 40-43 44-47 48-51
            };
            runnableStore.add(runnable);
            if (end >= iterations) run = false;
        }

        ExecutorService executor = Executors.newFixedThreadPool(systemCores);

        CountDownLatch latch = new CountDownLatch(runnableStore.size());

        runnableStore.forEach((task) -> {
            executor.submit(() -> {
                try {
                    task.run();
                } finally {
                    latch.countDown();
                }
            });
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        BigNumber finalSum = BigNumber.of("0", mc);
        for (BigNumber val : termStore) {
            finalSum = finalSum.add(val);
        }

        BigNumber r = finalSum.reciprocal().divide("12").multiply(BigNumber.of("640320", mc).multiply(BigNumber.of("640320", mc).squareRoot()));

        System.out.println(Functions.truncate(r, digits));
        System.out.println();
        System.out.println("Completed!");

    }

    public BigNumber calcTerm(long k, MathContext mc) {
        BigNumber u1 = BigNumber.of("-1", mc).power(String.valueOf(k));
        BigNumber u2 = BigNumber.of("6", mc).multiply(String.valueOf(k)).factorial();
        BigNumber u3 = BigNumber.of("545140134", mc).multiply(String.valueOf(k)).add("13591409");

        BigNumber l1 = BigNumber.of("3", mc).multiply(String.valueOf(k)).factorial();
        BigNumber l2 = BigNumber.of(String.valueOf(k), mc).factorial().power("3");
        BigNumber l3 = BigNumber.of("640320", mc).power(BigNumber.of("3", mc).multiply(String.valueOf(k)));

        return u1.multiply(u2).multiply(u3).divide(l1.multiply(l2).multiply(l3));
    }
}
