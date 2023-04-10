package threadpool;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import java.util.concurrent.Callable;

public class ThreadPool {

    /**
     * Fields
     */
    private final Queue<FutureTask<?>> priorityQueue = new ArrayDeque<>();
    private final Queue<FutureTask<?>> nonPriorityQueue = new ArrayDeque<>();
    private final Object lock = new Object();

    /**
     * Creates a new thread pool
     *
     * @param numberOfThreads the number of threads used
     */
    public ThreadPool(int numberOfThreads) {
        for (int i = 0; i < numberOfThreads; i++) {
            new Thread(() -> {
                while (true) {
                    FutureTask<?> futureTask = priorityQueue.poll();

                    //Check for a non-priority task if no priority task is available
                    if (futureTask == null) {
                        futureTask = nonPriorityQueue.poll();
                    }

                    //Executed the task found
                    if (futureTask != null) {
                        futureTask.run();
                    }

                    else {
                        try {
                            synchronized (lock) {
                                lock.wait();
                            }
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }).start();
        }
    }

    /**
     * Submit a priority task
     *
     * @param task The task
     * @return the result of the task being calculated asynchronously
     */
    public <T> Future<T> submitPriorityTask(Callable<T> task) {
        FutureTask<T> futureTask = new FutureTask<>(task);
        priorityQueue.offer(futureTask);
        synchronized (lock) {
            lock.notify();
        }
        return futureTask;
    }

    /**
     * Submit a non-priority task
     *
     * @param task The task
     * @return the result of the task being calculated asynchronously
     */
    public <T> Future<T> submitNonPriorityTask(Callable<T> task) {
        FutureTask<T> futureTask = new FutureTask<>(task);
        nonPriorityQueue.offer(futureTask);
        synchronized (lock) {
            lock.notify();
        }
        return futureTask;
    }

}