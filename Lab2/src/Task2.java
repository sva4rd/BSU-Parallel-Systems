import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Task2 {
    private final static int MAX = 100_000_000;
    private static final int[] array = new int[MAX];
    private static final Random rand = new Random();


    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        int numberOfThreads = 0;
        while (numberOfThreads < 1 || numberOfThreads > 30) {
            System.out.print("How many threads do you want to use (from 1 to 30)? ");
            numberOfThreads = scanner.nextInt();
            if (numberOfThreads < 1 || numberOfThreads > 30)
                System.out.println("Please enter a number between 1 and 30!");
        }

        for (int i = 0; i < MAX; i++) {
            array[i] = rand.nextInt(100);
        }

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        // Разделение списка на части для каждого потока
        int chunkSize = (int) Math.ceil((double) MAX / numberOfThreads);
        Future<Integer>[] futures = new Future[numberOfThreads];

        long start = System.currentTimeMillis();
        for (int i = 0; i < numberOfThreads; i++) {
            int startIdx = i * chunkSize;
            int endIdx = Math.min(MAX, startIdx + chunkSize);
            futures[i] = executor.submit(new SumTask(startIdx, endIdx));
        }

        int sum = 0;
        for (Future<Integer> future : futures) {
            sum += future.get();
        }
        long end = System.currentTimeMillis();

        executor.shutdown();

        long elapsedTimeMulti = end - start;
        System.out.println("Multithreaded program counted sum = " +
                sum + " in " + (elapsedTimeMulti / 1000.0) + " seconds.");


        // Исследование эффективности различных вариантов
        Integer[] integerArray = toIntegerArray(array);

        // Реализация 1: Коллекция List<Integer>
        start = System.currentTimeMillis();
        int sum1 = calculateSumUsingList(integerArray);
        end = System.currentTimeMillis();
        long elapsedTime1 = end - start;
        System.out.println("Using List<Integer> took " + (elapsedTime1 / 1000.0) + " seconds. Sum = " + sum1);

        // Реализация 2: Stream
        start = System.currentTimeMillis();
        int sum2 = Arrays.stream(integerArray).reduce(0, Integer::sum);
        end = System.currentTimeMillis();
        long elapsedTime2 = end - start;
        System.out.println("Using Stream took " + (elapsedTime2 / 1000.0) + " seconds. Sum = " + sum2);

        // Реализация 3: ParallelStream
        start = System.currentTimeMillis();
        int sum3 = Arrays.stream(integerArray).parallel().reduce(0, Integer::sum);
        end = System.currentTimeMillis();
        long elapsedTime3 = end - start;
        System.out.println("Using ParallelStream took " + (elapsedTime3 / 1000.0) + " seconds. Sum = " + sum3);
    }

    private static Integer[] toIntegerArray(int[] array) {
        Integer[] result = new Integer[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    private static int calculateSumUsingList(Integer[] array) {
        List<Integer> list = Arrays.asList(array);
        int sum = 0;
        for (int num : list) {
            sum += num;
        }
        return sum;
    }

    private static class SumTask implements Callable<Integer> {
        private final int startIdx;
        private final int endIdx;

        public SumTask(int startIdx, int endIdx) {
            this.startIdx = startIdx;
            this.endIdx = endIdx;
        }

        @Override
        public Integer call() throws Exception {
            int sum = 0;
            for (int i = startIdx; i < endIdx; i++) {
                sum += array[i];
            }
            return sum;
        }
    }
}
