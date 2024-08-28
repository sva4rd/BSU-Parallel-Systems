package pack;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

class CustomSet<T>{

    public void add(T element) {
    }

    public void remove(T element) {
    }

    public boolean contains(T element) {
        return false;
    }
}

class CoarseGrainedSet<T> extends CustomSet<T>{
    private Set<T> set = Collections.synchronizedSet(new HashSet<>());

    public void add(T element) {
        set.add(element);
    }

    public void remove(T element) {
        set.remove(element);
    }

    public boolean contains(T element) {
        return set.contains(element);
    }
}

class FineGrainedSet<T> extends CustomSet<T>{
    private final Map<T, Lock> locks = new ConcurrentHashMap<>();
    private Set<T> set = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private Lock getLock(T element) {
        locks.putIfAbsent(element, new ReentrantLock());
        return locks.get(element);
    }

    public void add(T element) {
        Lock lock = getLock(element);
        lock.lock();
        try {
            set.add(element);
        } finally {
            lock.unlock();
        }
    }

    public void remove(T element) {
        Lock lock = getLock(element);
        lock.lock();
        try {
            set.remove(element);
        } finally {
            lock.unlock();
        }
    }

    public boolean contains(T element) {
        Lock lock = getLock(element);
        lock.lock();
        try {
            return set.contains(element);
        } finally {
            lock.unlock();
        }
    }
}

public class SynchronizationComparison {
    private static final int NUM_THREADS = 10000;
    private static final int NUM_OPERATIONS = 1000;

    public static void main(String[] args) throws InterruptedException {
        CoarseGrainedSet<Integer> coarseGrainedSet = new CoarseGrainedSet<>();
        FineGrainedSet<Integer> fineGrainedSet = new FineGrainedSet<>();

        long coarseGrainedTime = testSetPerformance(coarseGrainedSet);
        long fineGrainedTime = testSetPerformance(fineGrainedSet);

        System.out.println("Coarse-grained set time: " + coarseGrainedTime + " ms");
        System.out.println("Fine-grained set time: " + fineGrainedTime + " ms");
    }

    private static long testSetPerformance(CustomSet<Integer> set) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < NUM_THREADS; i++) {
            executor.submit(() -> {
                for (int j = 0; j < NUM_OPERATIONS; j++) {
                    set.add(j);
                    set.contains(j);
                    set.remove(j);
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);
        return System.currentTimeMillis() - startTime;
    }
}