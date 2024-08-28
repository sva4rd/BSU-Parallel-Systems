import java.util.Arrays;
import java.util.Scanner;

public class Task1 {
    private final static int MAX = 10_000_000;
    private final static int cycle = 10000;

    private static class CountPrimesThread extends Thread {
        private final int id;
        private int count;
        private final int start;
        private final int threadsNum;
        private final boolean[] isPrimeArray;

        public CountPrimesThread(int id, int start, int threadsNum) {
            this.id = id;
            this.count = 0;
            this.start = start;
            this.threadsNum = threadsNum;
            this.isPrimeArray = new boolean[cycle + 1];
        }

        public int getCount() {
            return count;
        }

        public void run() {
            long startTime = System.currentTimeMillis();
            int i;
            for (i = start; i < MAX - cycle; i += cycle * threadsNum) {
                count += countPrimes(i, i + cycle, isPrimeArray);
            }
            if (i - MAX < 0) {
                count += countPrimes(i, MAX + 1, isPrimeArray);
            }
            long elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println("Thread " + id + " counted " +
                    count + " primes in " + (elapsedTime / 1000.0) + " seconds.");
        }
    }

    public static void main(String[] args) {
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
        int countSeq = countPrimes(2, MAX + 1, new boolean[MAX + 1]);
        long elapsedTimeSeq = System.currentTimeMillis() - startTimeSeq;
        System.out.println("Sequential program counted " +
                countSeq + " primes in " + (elapsedTimeSeq / 1000.0) + " seconds.");

        // Multithreaded program
        System.out.println("\nRunning multithreaded program with " + numberOfThreads + " threads...");
        CountPrimesThread[] worker = new CountPrimesThread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            int start = i * cycle + 2;
            worker[i] = new CountPrimesThread(i, start, numberOfThreads);
        }

        long startTimeMulti = System.currentTimeMillis();
        for (int i = 0; i < numberOfThreads; i++)
            worker[i].start();

        for (int i = 0; i < numberOfThreads; i++) {
            try {
                worker[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        int countMulti = 0;
        for (int i = 0; i < numberOfThreads; i++)
            countMulti += worker[i].getCount();

        long elapsedTimeMulti = System.currentTimeMillis() - startTimeMulti;
        System.out.println("Multithreaded program counted " +
                countMulti + " primes in " + (elapsedTimeMulti / 1000.0) + " seconds.");
    }

    private static int countPrimes(int start, int end, boolean[] isPrime) {
        Arrays.fill(isPrime, true);
        int count = 0;
        for (int i = 2; i * i <= end; i++) {
            if (i >= start && !isPrime[i - start]) continue;
            for (int j = Math.max(i * i, (start + i - 1) / i * i); j <= end; j += i) {
                isPrime[j - start] = false;
            }
        }
        for (int i = Math.max(start, 2); i < end; i++) {
            if (isPrime[i - start]) count++;
        }
        return count;
    }

}
