package top.kwseeker.concurrency.threadlocal;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SimplestExample {

    public static void main(String[] args) throws InterruptedException {
        //假设一个线程池有两个线程，执行10个任务，每个任务0-2s处理时间，线程执行完一个任务，统计完成任务数+1
        ExecutorService executor = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 10; i++) {
            executor.execute(new Task());
        }
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }

    //线程间不共享所以绝对线程安全
    static class TLHolder {
        private static final ThreadLocal<Integer> tc = ThreadLocal.withInitial(() -> 0);

        public static int get() {
            return tc.get();
        }

        public static void set(int value) {
            tc.set(value);
        }

        public static int incrementAndGet() {
            int count = tc.get() + 1;
            tc.set(count);
            return count;
        }

        public static void remove() {
            tc.remove();
        }
    }

    static class Task implements Runnable {
        @Override
        public void run() {
            Random random = new Random();
            int costTime = random.nextInt(1000);
            try {
                Thread.sleep(costTime);
            } catch (InterruptedException ignored) {
            }
            System.out.println("Thread(" + Thread.currentThread().getName() + "): " + TLHolder.incrementAndGet());
        }
    }
}