package pack;

import java.util.concurrent.*;

class Point {
    double x, y;

    Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
}

class Producer extends Thread {
    private final BlockingQueue<Point> queue;
    private final int numPoints;

    Producer(BlockingQueue<Point> queue, int numPoints) {
        this.queue = queue;
        this.numPoints = numPoints;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < numPoints; i++) {
                double x = Math.random();
                double y = Math.random();
                queue.put(new Point(x, y));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class Consumer extends Thread {
    private final BlockingQueue<Point> queue;
    private int insideCircle = 0;

    Consumer(BlockingQueue<Point> queue) {
        this.queue = queue;
    }

    public int getInsideCircle(){
        return insideCircle;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Point point = queue.take();
                if (point.x * point.x + point.y * point.y <= 1) {
                    insideCircle++;
                }
            }
        } catch (InterruptedException ignored) {
        }
    }
}

public class MonteCarloPi {
    public static void main(String[] args) throws InterruptedException {
        int numPoints = 1_000_000;
        BlockingQueue<Point> queue = new ArrayBlockingQueue<>(numPoints);
        Producer[] producers = new Producer[2];
        Consumer[] consumers = new Consumer[2];

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < producers.length; i++) {
            producers[i] = new Producer(queue, numPoints / producers.length);
            consumers[i] = new Consumer(queue);
            producers[i].start();
            consumers[i].start();
        }

        for (Producer producer : producers) {
            producer.join();
        }

        int totalInsideCircle = 0;
        for (Consumer consumer : consumers) {
            totalInsideCircle += consumer.getInsideCircle();
            consumer.interrupt();
        }

        long endTime = System.currentTimeMillis();
        Thread.sleep(500);
        System.out.println("Final estimation of Pi: " + 4 * (double) totalInsideCircle / (numPoints));
        System.out.println("Total execution time: " + (endTime - startTime) / 1000.0 + "s");
    }
}
