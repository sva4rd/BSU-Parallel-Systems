package pack;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CountPrimes {
    private static boolean isPrime(int x) {
        if (x <= 1) return false;
        int top = (int) Math.sqrt(x);
        for (int i = 2; i <= top; i++)
            if (x % i == 0)
                return false;
        return true;
    }

    private static List<Integer> countPrimes(int start, int end) {
        return IntStream.rangeClosed(start, end)
                .filter(CountPrimes::isPrime)
                .boxed()
                .collect(Collectors.toList());
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        int MIN = 1;
        int MAX = 100_000_000;
        Scanner scanner = new Scanner(System.in);
        int numThreads = 0;
        while (numThreads < 1 || numThreads > 30) {
            System.out.print("How many threads do you want to use (from 1 to 30)? ");
            numThreads = scanner.nextInt();
            if (numThreads < 1 || numThreads > 30)
                System.out.println("Please enter a number between 1 and 30!");
        }

        // Sequential program
//        long startTimeSeq = System.currentTimeMillis();
//        List<Integer> primesSeq = countPrimes(MIN, MAX);
//        long elapsedTimeSeq = System.currentTimeMillis() - startTimeSeq;
//        System.out.println("Sequential program counted " +
//                primesSeq.size() + " primes in " + (elapsedTimeSeq / 1000.0) + "s");


        // Multithreaded program
        int step = (MAX - MIN + 1) / numThreads;
        List<CompletableFuture<List<Integer>>> futures = new ArrayList<>();

        long startTimeMulti = System.currentTimeMillis();
        for (int i = 0; i < numThreads; i++) {
            int start = MIN + i * step;
            int end = (i == numThreads - 1) ? MAX : start + step - 1;
            futures.add(CompletableFuture.supplyAsync(() -> countPrimes(start, end)));
        }
        List<Integer> primes = new ArrayList<>();
        for (CompletableFuture<List<Integer>> future : futures)
            primes.addAll(future.get());

        long elapsedTimeMulti = System.currentTimeMillis() - startTimeMulti;
        System.out.println("Multithreaded program counted " +
                primes.size() + " primes in " + (elapsedTimeMulti / 1000.0) + "s");
    }
}

