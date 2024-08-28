package pack;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите количество потоков: ");
        int numberOfThreads = scanner.nextInt();
        scanner.close();

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        Path inputPath = Paths.get("input.txt");
        Path outputPath = Paths.get("output.txt");
        Map<Integer, Long> threadTimes = new ConcurrentHashMap<>();

        long startTimeProg = System.currentTimeMillis();
        try (BufferedReader reader = Files.newBufferedReader(inputPath);
             BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            List<Future<String>> futures = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String finalLine = line;
                Future<String> future = executor.submit(() -> {
                    long startTime = System.nanoTime();
                    String[] words = finalLine.split("\\s+");
                    StringBuilder reversedLine = new StringBuilder();
                    for (String word : words) {
                        reversedLine.append(new StringBuilder(word).reverse().toString()).append(" ");
                    }
                    long endTime = System.nanoTime();
                    threadTimes.merge((int) Thread.currentThread().getId(), endTime - startTime, Long::sum);
                    return reversedLine.toString();
                });
                futures.add(future);
            }

            for (Future<String> future : futures) {
                writer.write(future.get());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);

            threadTimes.forEach((id, time) -> System.out.println("Поток " + id + " работал " + time / 1_000_000 + " мс"));
        }
        long elapsedTime = System.currentTimeMillis() - startTimeProg;
        System.out.println("Общее время работы программы: " + elapsedTime + " мс");
    }
}