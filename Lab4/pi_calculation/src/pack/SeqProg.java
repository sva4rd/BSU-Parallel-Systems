package pack;

public class SeqProg {
    public static void main(String[] args) {
        int numPoints = 1_000_000;
        int insideCircle = 0;

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numPoints; i++) {
            double x = Math.random();
            double y = Math.random();
            if (x * x + y * y <= 1)
                insideCircle++;
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Final estimation of Pi: " + 4 * (double) insideCircle / numPoints);
        System.out.println("Total execution time: " + (endTime - startTime) / 1000.0);
    }
}
