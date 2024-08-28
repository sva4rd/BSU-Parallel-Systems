import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;

public class Main_2 {
    private final static int MAX = 10_000_000;
    private final static int cycle = 100;

    private static class CountPrimesTask implements Callable<Integer> {
        private final int id;
        private int count;
        private final int start;
        private final int threadsNum;

        public CountPrimesTask(int id, int start, int threadsNum) {
            this.id = id;
            this.count = 0;
            this.start = start;
            this.threadsNum = threadsNum;
        }

        @Override
        public Integer call() {
            long startTime = System.currentTimeMillis();
            int i;
            for (i = start; i < MAX - cycle; i += cycle * threadsNum)
                count += countPrimes(i, i + cycle);
            if (i + cycle - MAX < 100)
                count += countPrimes(i, MAX + 1);
            long elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println("Thread " + id + " counted " +
                    count + " primes in " + (elapsedTime / 1000.0) + " seconds.");
            return count;
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        int numberOfThreads = 0;
        while (numberOfThreads < 1 || numberOfThreads > 30) {
            System.out.print("How many threads do you want to use (from 1 to 30)? ");
            numberOfThreads = scanner.nextInt();
            if (numberOfThreads < 1 || numberOfThreads > 30)
                System.out.println("Please enter a number between 1 and 30!");
        }

        // Sequential program
        System.out.println("\nRunning sequential program...");
        long startTimeSeq = System.currentTimeMillis();
        int countSeq = countPrimes(2, MAX + 1);
        long elapsedTimeSeq = System.currentTimeMillis() - startTimeSeq;
        System.out.println("Sequential program counted " +
                countSeq + " primes in " + (elapsedTimeSeq / 1000.0) + " seconds.");

        // Multithreaded program
        System.out.println("\nRunning multithreaded program with " + numberOfThreads + " threads...");
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        Future<Integer>[] futures = new Future[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            int start = i * cycle + 2;
            futures[i] = executor.submit(new CountPrimesTask(i, start, numberOfThreads));
        }

        long startTimeMulti = System.currentTimeMillis();
        int countMulti = 0;
        for (int i = 0; i < numberOfThreads; i++)
            countMulti += futures[i].get();

        long elapsedTimeMulti = System.currentTimeMillis() - startTimeMulti;
        System.out.println("Multithreaded program counted " +
                countMulti + " primes in " + (elapsedTimeMulti / 1000.0) + " seconds.");

        executor.shutdown();
    }

    private static int countPrimes(int start, int end) {
        int count = 0;
        for (int i = start; i < end; i += 1)
            if (isPrime(i))
                count++;
        return count;
    }

    private static boolean isPrime(int x) {
        assert x > 1;
        int top = (int) Math.sqrt(x);
        for (int i = 2; i <= top; i++)
            if (x % i == 0)
                return false;
        return true;
    }
}
