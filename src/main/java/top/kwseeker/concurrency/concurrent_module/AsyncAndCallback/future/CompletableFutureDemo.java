package top.kwseeker.concurrency.concurrent_module.AsyncAndCallback.future;

import java.time.LocalDate;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class CompletableFutureDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //CompletableFuture<String> completableFuture = new CompletableFuture<>();
        //new Thread(() -> {
        //    try {
        //        Thread.sleep(3000);
        //        String message = "Hello Arvin";
        //        completableFuture.complete(message);
        //    } catch (InterruptedException e) {
        //        e.printStackTrace();
        //    }
        //}).start();
        //String result = completableFuture.get();    //阻塞获取结果（同步）
        //System.out.println(result);

        CompletableFuture<Void> asyncCompletableFuture = CompletableFuture.runAsync(() -> { //相当于Thread运行Runnable
            System.out.println("Hello Arvin");
        });

        ExecutorService executor = Executors.newFixedThreadPool(1);
        CompletableFuture<Void> asyncCompletableFuture1 = CompletableFuture.runAsync(() -> { //相当于线程池运行Runnable
            System.out.println("Hello world");
        }, executor);
        asyncCompletableFuture1.get();  //阻塞等待执行完成
        executor.shutdown();

        CompletableFuture<String> asyncCompletableFuture2 = CompletableFuture.supplyAsync(new Supplier<String>() {
            @Override
            public String get() {
                return "Hello Lee";
            }
        });
        String result = asyncCompletableFuture2.get();  //仍然是阻塞等待
        System.out.println(result);


        CompletableFuture asyncCompletableFuture3 = CompletableFuture.supplyAsync(() -> {
            return String.format("[Thread: %s] Hello world ...", Thread.currentThread().getName());
        }).thenApply(value -> {
            return value + " at " + LocalDate.now();
        }).thenApply(value -> {
            System.out.println(value);
            return value;                               //异步返回结果 + 合并Future结果
        }).thenRun(() -> {
            System.out.println("操作结束");
        }).exceptionally((e)-> {                        //异常处理
            e.printStackTrace();
            return null;
        });

        //while (!asyncCompletableFuture3.isDone()) {
        //    Thread.yield();
        //}
        System.out.println(asyncCompletableFuture3.get());  //仍然 是阻塞等待
    }
}
