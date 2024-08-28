//import java.util.Scanner;
//
//public class Main {
//    private final static int MAX = 10_000_000;
//
//    private static class CountPrimesThread extends Thread {
//        int id;  // An id number for this thread; specified in the constructor.
//        public CountPrimesThread(int id) {
//            this.id = id;
//        }
//        public void run() {
//            long startTime = System.currentTimeMillis();
//            int count = countPrimes(2,MAX);
//            long elapsedTime = System.currentTimeMillis() - startTime;
//            System.out.println("Thread " + id + " counted " +
//                    count + " primes in " + (elapsedTime/1000.0) + " seconds.");
//        }
//    }
//
//
//    public static void main(String[] args) {
//        Scanner scanner = new Scanner(System.in);
//        int numberOfThreads = 0;
//        while (numberOfThreads < 1 || numberOfThreads > 30) {
//            System.out.print("How many threads do you want to use  (from 1 to 30) ?  ");
//            numberOfThreads = scanner.nextInt();
//            if (numberOfThreads < 1 || numberOfThreads > 30)
//                System.out.println("Please enter a number between 1 and 30 !");
//        }
//        System.out.println("\nCreating " + numberOfThreads + " prime-counting threads...");
//        CountPrimesThread[] worker = new CountPrimesThread[numberOfThreads];
//        for (int i = 0; i < numberOfThreads; i++)
//            worker[i] = new CountPrimesThread( i );
//        for (int i = 0; i < numberOfThreads; i++)
//            worker[i].start();
//        System.out.println("Threads have been created and started.");
//    }
//
//    private static int countPrimes(int min, int max) {
//        int count = 0;
//        for (int i = min; i <= max; i++)
//            if (isPrime(i))
//                count++;
//        return count;
//    }
//
//    private static boolean isPrime(int x) {
//        assert x > 1;
//        int top = (int)Math.sqrt(x);
//        for (int i = 2; i <= top; i++)
//            if ( x % i == 0 )
//                return false;
//        return true;
//    }
//}




//import java.util.Scanner;
//
//public class Main {
//    private final static int MAX = 10_000_000;
//
//    private static class CountPrimesThread extends Thread {
//        private int id; // An id number for this thread; specified in the constructor.
//        private int start; // The starting number for this thread's range.
//        private int end; // The ending number for this thread's range.
//        private int count; // The count of prime numbers found by this thread.
//
//        public CountPrimesThread(int id, int start, int end) {
//            this.id = id;
//            this.start = start;
//            this.end = end;
//            this.count = 0;
//        }
//
//        public int getCount() {
//            return count;
//        }
//
//        public void run() {
//            long startTime = System.currentTimeMillis();
//            count = countPrimes(start, end);
//            long elapsedTime = System.currentTimeMillis() - startTime;
//            System.out.println("Thread " + id + " counted " +
//                    count + " primes in " + (elapsedTime / 1000.0) + " seconds.");
//        }
//    }
//
//    public static void main(String[] args) {
//        Scanner scanner = new Scanner(System.in);
//        int numberOfThreads = 0;
//        while (numberOfThreads < 1 || numberOfThreads > 30) {
//            System.out.print("How many threads do you want to use (from 1 to 30)? ");
//            numberOfThreads = scanner.nextInt();
//            if (numberOfThreads < 1 || numberOfThreads > 30)
//                System.out.println("Please enter a number between 1 and 30!");
//        }
//
//        // Sequential program
//        System.out.println("\nRunning sequential program...");
//        long startTimeSeq = System.currentTimeMillis();
//        int countSeq = countPrimes(2, MAX);
//        long elapsedTimeSeq = System.currentTimeMillis() - startTimeSeq;
//        System.out.println("Sequential program counted " +
//                countSeq + " primes in " + (elapsedTimeSeq / 1000.0) + " seconds.");
//
//        // Multithreaded program
//        System.out.println("\nRunning multithreaded program with " + numberOfThreads + " threads...");
//        CountPrimesThread[] worker = new CountPrimesThread[numberOfThreads];
//        int range = MAX / numberOfThreads;
//        for (int i = 0; i < numberOfThreads; i++) {
//            int start = i * range + 2;
//            int end = (i == numberOfThreads - 1) ? MAX : (i + 1) * range + 1;
//            worker[i] = new CountPrimesThread(i, start, end);
//        }
//
//        long startTimeMulti = System.currentTimeMillis();
//        for (int i = 0; i < numberOfThreads; i++)
//            worker[i].start();
//
//        // Wait for all threads to finish
//        for (int i = 0; i < numberOfThreads; i++) {
//            try {
//                worker[i].join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//        int countMulti = 0;
//        for (int i = 0; i < numberOfThreads; i++)
//            countMulti += worker[i].getCount();
//
//        long elapsedTimeMulti = System.currentTimeMillis() - startTimeMulti;
//        System.out.println("Multithreaded program counted " +
//                countMulti + " primes in " + (elapsedTimeMulti / 1000.0) + " seconds.");
//    }
//
//    private static int countPrimes(int min, int max) {
//        int count = 0;
//        for (int i = min; i <= max; i++)
//            if (isPrime(i))
//                count++;
//        return count;
//    }
//
//    private static boolean isPrime(int x) {
//        assert x > 1;
//        int top = (int) Math.sqrt(x);
//        for (int i = 2; i <= top; i++)
//            if (x % i == 0)
//                return false;
//        return true;
//    }
//}

import java.util.Scanner;

public class Main {
    private final static int MAX = 1_000_000;
    private final static int cycle = 100;

    private static class CountPrimesThread extends Thread {
        private final int id;
        private int count;
        private final int start;
        private final int threadsNum;

        public CountPrimesThread(int id, int start, int threadsNum) {
            this.id = id;
            this.count = 0;
            this.start = start;
            this.threadsNum = threadsNum;
        }

        public int getCount() {
            return count;
        }

        public void run() {
            long startTime = System.currentTimeMillis();
            int i;
            for (i = start; i < MAX - cycle; i += cycle * threadsNum)
                count += countPrimes(i, i + cycle);
            if (i + cycle - MAX < 100)
                count += countPrimes(i, MAX + 1);
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
        int countSeq = countPrimes(2, MAX + 1);
        long elapsedTimeSeq = System.currentTimeMillis() - startTimeSeq;
        System.out.println("Sequential program counted " +
                countSeq + " primes in " + (elapsedTimeSeq / 1000.0) + " seconds.");

        // Multithreaded program
        System.out.println("\nRunning multithreaded program with " + numberOfThreads + " threads...");
        long startTimeMulti = System.currentTimeMillis();
        CountPrimesThread[] worker = new CountPrimesThread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            int start = i * cycle + 2;
            worker[i] = new CountPrimesThread(i, start, numberOfThreads);
        }

        //long startTimeMulti = System.currentTimeMillis();
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