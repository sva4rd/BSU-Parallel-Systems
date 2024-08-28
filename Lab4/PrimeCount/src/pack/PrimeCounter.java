package pack;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Worker implements Runnable {
    private final int id;
    private final int max;
    private final AtomicInteger currentNum;
    private static final AtomicInteger primeCount = new AtomicInteger();

    Worker(int id, AtomicInteger number, int max) {
        this.id = id;
        this.currentNum = number;
        this.max = max;
    }

    public static int getCount() {
        return primeCount.get();
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        while (!Thread.currentThread().isInterrupted()) {
            int number = currentNum.getAndIncrement();
            if (number >= max) break;
            if (isPrime(number)) {
                primeCount.incrementAndGet();
            }
        }
        long elapsedTime = System.currentTimeMillis() - startTime;
        System.out.println("Thread " + id + " finished in " + (elapsedTime / 1000.0) + " seconds.");

    }

    private boolean isPrime(int number) {
        if (number <= 1) return false;
        for (int i = 2; i * i <= number; i++) {
            if (number % i == 0) return false;
        }
        return true;
    }
}

public class PrimeCounter {
    public static void main(String[] args) {
        int max = 10_000_000;
        AtomicInteger counter = new AtomicInteger();

        Scanner scanner = new Scanner(System.in);
        int numberOfThreads = 0;
        while (numberOfThreads < 1 || numberOfThreads > 30) {
            System.out.print("How many threads do you want to use (from 1 to 30)? ");
            numberOfThreads = scanner.nextInt();
            if (numberOfThreads < 1 || numberOfThreads > 30)
                System.out.println("Please enter a number between 1 and 30!");
        }
        scanner.close();

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < numberOfThreads; i++)
            executor.submit(new Worker(i, counter, max));
        executor.shutdown();
        while (!executor.isTerminated()) ;
        long endTime = System.currentTimeMillis();
        System.out.println("Program counted " + Worker.getCount() + " primes in "
                + (endTime - startTime) / 1000.0 + " seconds.");
    }
}
