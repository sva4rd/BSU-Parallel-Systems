package pack;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.Semaphore;

public class Main_sem {
    private static final Semaphore semaphore = new Semaphore(1);

    public static void main(String[] args) {
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
             BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardOpenOption.APPEND)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String finalLine = line;
                executor.submit(() -> {
                    long startTime = System.nanoTime();
                    String[] words = finalLine.split("\\s+");
                    StringBuilder reversedLine = new StringBuilder();
                    for (String word : words) {
                        reversedLine.append(new StringBuilder(word).reverse().toString()).append(" ");
                    }
                    long endTime = System.nanoTime();
                    threadTimes.merge((int) Thread.currentThread().getId(), endTime - startTime, Long::sum);

                    try {
                        semaphore.acquire();
                        try {
                            writer.write(reversedLine.toString());
                            writer.newLine();
                            writer.flush();
                        } catch(IOException e){
                            throw new RuntimeException(e);
                        } finally {
                            semaphore.release();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }

            // Выводим общее время работы каждого потока
            threadTimes.forEach((id, time) -> System.out.println("Поток " + id + " работал " + time / 1_000_000 + " мс"));
        }
        long elapsedTime = System.currentTimeMillis() - startTimeProg;
        System.out.println("Общее время работы программы: " + elapsedTime + " мс");
    }
}