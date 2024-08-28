package pack;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class Main_async {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите количество потоков: ");
        int numberOfThreads = scanner.nextInt();
        scanner.close();

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        Path inputPath = Paths.get("input.txt");
        Path outputPath = Paths.get("output.txt");

        try (AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(outputPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
            Map<Integer, Long> threadTimes = new ConcurrentHashMap<>();
            AtomicLong filePosition = new AtomicLong(0);

            long startTimeProg = System.currentTimeMillis();
            try (BufferedReader reader = Files.newBufferedReader(inputPath)) {
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

                        ByteBuffer buffer = ByteBuffer.wrap((reversedLine.toString() + "\n").getBytes(StandardCharsets.UTF_8));
                        long position = filePosition.getAndAdd(buffer.limit());

                        fileChannel.write(buffer, position);
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
            }
            threadTimes.forEach((id, time) -> System.out.println("Поток " + id + " работал " + time / 1_000_000 + " мс"));
            long elapsedTime = System.currentTimeMillis() - startTimeProg;
            System.out.println("Общее время работы программы: " + elapsedTime + " мс");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}