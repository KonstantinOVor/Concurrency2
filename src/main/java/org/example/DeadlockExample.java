package org.example;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DeadlockExample {

    private static class Resource {
        // Ресурсы
    }

    private final Resource resourceA = new Resource();
    private final Resource resourceB = new Resource();
    private final Lock lockA = new ReentrantLock();
    private final Lock lockB = new ReentrantLock();

    public void execute() {
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                acquireResourcesAndWork(lockA, lockB, resourceA, resourceB, "Thread-1");
            }
        });

        Thread thread2 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                acquireResourcesAndWork(lockB, lockA, resourceB, resourceA, "Thread-2");
            }
        });

        thread1.start();
        thread2.start();
    }

    private void acquireResourcesAndWork(Lock firstLock, Lock secondLock, Resource firstResource, Resource secondResource, String threadName) {
        boolean lockedFirst = false;
        boolean lockedSecond = false;

        try {
            lockedFirst = firstLock.tryLock(100, TimeUnit.MILLISECONDS);
            if (lockedFirst) {
                System.out.println(threadName + " locked " + firstResource);

                try {
                    // Имитация работы с ресурсом
                    Thread.sleep(100);

                    lockedSecond = secondLock.tryLock(100, TimeUnit.MILLISECONDS);
                    if (lockedSecond) {
                        System.out.println(threadName + " locked " + secondResource);

                        try {
                            // Имитация работы с ресурсом
                            Thread.sleep(100);
                        } finally {
                            secondLock.unlock();
                            System.out.println(threadName + " unlocked " + secondResource);
                        }
                    }
                } finally {
                    if (lockedSecond) {
                        firstLock.unlock();
                        System.out.println(threadName + " unlocked " + firstResource);
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (lockedFirst && !lockedSecond) {
                firstLock.unlock();
                System.out.println(threadName + " unlocked " + firstResource);
            }
        }
    }

    public static void main(String[] args) {
        DeadlockExample example = new DeadlockExample();
        example.execute();
    }
}