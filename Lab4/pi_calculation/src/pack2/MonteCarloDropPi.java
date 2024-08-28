package pack2;

import java.util.concurrent.*;

class Point {
    double x, y;

    Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
}

class Drop {
    private Point point;
    private boolean empty = true;

    public synchronized Point take() {
        while (empty) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
        empty = true;
        notifyAll();
        return point;
    }

    public synchronized void put(Point point) {
        while (!empty) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
        empty = false;
        this.point = point;
        notifyAll();
    }
}

class Producer extends Thread {
    private final Drop drop;
    private final int numPoints;

    Producer(Drop drop, int numPoints) {
        this.drop = drop;
        this.numPoints = numPoints;
    }

    @Override
    public void run() {
        for (int i = 0; i < numPoints; i++) {
            double x = Math.random();
            double y = Math.random();
            drop.put(new Point(x, y));
        }
    }
}

class Consumer extends Thread {
    private final Drop drop;
    private int insideCircle = 0;

    Consumer(Drop drop) {
        this.drop = drop;
    }

    public int getInsideCircle(){
        return insideCircle;
    }

    @Override
    public void run() {
        while (true) {
            Point point = drop.take();
            if (point.x * point.x + point.y * point.y <= 1) {
                insideCircle++;
            }
        }
    }
}

public class MonteCarloDropPi {
    public static void main(String[] args) throws InterruptedException {
        int numPoints = 1_000_000;
        Drop drop = new Drop();
        Producer[] producers = new Producer[2];
        Consumer[] consumers = new Consumer[2];

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < producers.length; i++) {
            producers[i] = new Producer(drop, numPoints / producers.length);
            consumers[i] = new Consumer(drop);
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
