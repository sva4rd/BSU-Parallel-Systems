import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Task3 {
    private static final String SEARCH_STRING = "example";
    private static final String OUTPUT_FILE = "result.txt";

    public static void main(String[] args) {
        clearResultFile();
        List<String> fileNames = getFileNames();

        // Последовательная программа
        long sequentialTime = measureExecutionTime(() -> sequentialSearch(fileNames));

        // Многопоточная программа, Модель делегирования 1
        long delegateModel1Time = measureExecutionTime(() -> delegateModel1Search(fileNames));

        // Многопоточная программа, Модель делегирования 2
        long delegateModel2Time = measureExecutionTime(() -> delegateModel2Search(fileNames));

        System.out.println("sequentialSearch - " + sequentialTime / 1000.0 + " sec");
        System.out.println("delegateModel1 - " + delegateModel1Time / 1000.0 + " sec");
        System.out.println("delegateModel2 - " + delegateModel2Time / 1000.0 + " sec");
    }

    private static long measureExecutionTime(Runnable task) {
        long startTime = System.currentTimeMillis();
        task.run();
        return System.currentTimeMillis() - startTime;
    }

    private static List<String> getFileNames() {
        List<String> fileNames = new ArrayList<>();
        for (int i = 1; i <= 10; i++)
            fileNames.add("data/file" + i + ".txt");
        return fileNames;
    }

    private static void sequentialSearch(List<String> fileNames) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(OUTPUT_FILE, true))) {
            for (String fileName : fileNames) {
                List<Integer> lineNumbers = searchFile(fileName);
                writeResult(writer, fileName, lineNumbers);
            }
            writer.println("Sequential search completed.\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void delegateModel1Search(List<String> fileNames) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(OUTPUT_FILE, true))) {
            ExecutorService executor = Executors.newCachedThreadPool();
            for (String fileName : fileNames) {
                executor.execute(() -> {
                    List<Integer> lineNumbers = null;
                    try {
                        lineNumbers = searchFile(fileName);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    synchronized (writer) {
                        writeResult(writer, fileName, lineNumbers);
                    }
                });
            }
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            writer.println("Delegate Model 1 search completed.\n");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void delegateModel2Search(List<String> fileNames) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(OUTPUT_FILE, true))) {
            ExecutorService executor = Executors.newFixedThreadPool(4);
            for (String fileName : fileNames) {
                executor.execute(new SearchWorker(fileName, writer));
            }
            executor.shutdown();
            while (!executor.isTerminated()) ;
            writer.println("Delegate Model 2 search completed.\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Integer> searchFile(String fileName) throws IOException {
        List<Integer> lineNumbers = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                if (line.contains(SEARCH_STRING)) {
                    lineNumbers.add(lineNumber);
                }
                lineNumber++;
            }
        }
        return lineNumbers;
    }

    private static void writeResult(PrintWriter writer, String fileName, List<Integer> lineNumbers) {
        writer.print(fileName + " ");
        for (int lineNumber : lineNumbers) {
            writer.print(lineNumber + " ");
        }
        if (lineNumbers.isEmpty())
            writer.print("-");
        writer.println();
    }

    private static void clearResultFile() {
        try (PrintWriter writer = new PrintWriter(OUTPUT_FILE)) {
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static class SearchWorker implements Runnable {
        private final String fileName;
        private final PrintWriter writer;

        public SearchWorker(String fileName, PrintWriter writer) {
            this.fileName = fileName;
            this.writer = writer;
        }

        @Override
        public void run() {
            try {
                List<Integer> lineNumbers = searchFile(fileName);
                synchronized (writer) {
                    writeResult(writer, fileName, lineNumbers);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}