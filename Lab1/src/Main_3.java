import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main_3 {
    private static volatile boolean stopCalculations = false;

    public static void main(String[] args) {
        ExecutorService calculationExecutor = Executors.newSingleThreadExecutor();
        ExecutorService userInputExecutor = Executors.newSingleThreadExecutor();

        calculationExecutor.submit(Main_3::calculate);
        userInputExecutor.submit(Main_3::getUserInput);

        calculationExecutor.shutdown();
        userInputExecutor.shutdown();
        while (!calculationExecutor.isTerminated() || !userInputExecutor.isTerminated()) {
            if (stopCalculations) {
                userInputExecutor.shutdownNow();
                break;
            }
        }
    }

    private static void calculate() {
        System.out.println("Started calculating the number of prime numbers on the diapason [2, 50_000_000]:");
        int count = 0;
        for (int i = 2; i <= 50_000_000; i += 1) {
            if (isPrime(i))
                count++;
            if (stopCalculations) {
                System.out.println("The calculations were prematurely completed");
                break;
            }
        }
        if (!stopCalculations)
            System.out.println("The calculations are fully completed");
        System.out.println("Result: " + count);
        stopCalculations = true;
    }

    private static boolean isPrime(int x) {
        assert x > 1;
        int top = (int) Math.sqrt(x);
        for (int i = 2; i <= top; i++)
            if (x % i == 0)
                return false;
        return true;
    }

    private static void getUserInput() {
        System.out.println("Press \"Enter\" to end the calculation prematurely");
        try (Scanner scanner = new Scanner(System.in)) {
            while (!stopCalculations) {
                while (System.in.available() == 0 && !stopCalculations)
                    Thread.sleep(200);

                if (stopCalculations)
                    break;

                scanner.nextLine();
                stopCalculations = true;
            }
        } catch (InterruptedException | IOException ignored) {}
    }
}
