package top.kwseeker.concurrency.thread;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * 父线程捕获子线程的异常
 * 父线程无法直接通过 try-catch 捕获子线程抛出的异常
 * 但是可以通过3种方法来捕获子线程的异常
 * 1. Callable + Future
 *    以 FutureTask 为例，之所以可以通过 get() 获取子线程的异常是因为 FutureTask 通过结果字段 outcome 传递出了子线程的异常信息
 *    详细参考 FutureTask$report(int s) 方法
 * 2. UncaughtExceptionHandler
 *    为线程设置一个异常处理器，当子线程抛出异常时，回调异常处理方法。
 *    不过已经不能算作父线程捕获子线程异常了，实际还是子线程自己捕获的。
 * 3. CompletableFuture
 *    和 FutureTask 原理一样，通过结果字段 (result 字段) 传递异常给调用 get() 方法的线程
 */
public class ParentThreadCatchExceptionInChild {

    private static final Thread.UncaughtExceptionHandler handler = (t, e) ->
            System.out.println("UncaughtExceptionHandler caught exception: " + e.getMessage());

    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2, 2, 60, TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(100),
            new ThreadFactoryBuilder()
                    .setNameFormat("thread-%d")
                    .setUncaughtExceptionHandler(handler).build()
    );

    public static void main(String[] args) throws InterruptedException {
        testFuture();
        //testCallableFuture();
        //testUncaughtExceptionHandler();
    }

    private static void testFuture() throws InterruptedException {
        FutureTask<Void> task = new FutureTask<Void>(() -> {
            throw new RuntimeException("Exception threw by thread");
        });
        Thread thread = new Thread(task);
        thread.start();
        try {
            task.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        thread.join();
    }

    /**
     * 同时有设置 UncaughtExceptionHandler 的话，异常优先被传递
     */
    private static void testCallableFuture() {
        Future<Object> future = executor.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                throw new Exception("Exception threw by callable");
            }
        });
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static void testUncaughtExceptionHandler() throws InterruptedException {
        Thread thread = new Thread(() -> {
            throw new RuntimeException("Exception threw by thread");
        });
        thread.setUncaughtExceptionHandler(handler);
        thread.start();

        thread.join();
    }

    private static void testCompletableFuture() throws InterruptedException {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            throw new RuntimeException("Exception threw by thread");
        });
        try {
            future.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
