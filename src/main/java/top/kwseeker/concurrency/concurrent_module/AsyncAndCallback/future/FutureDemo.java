package top.kwseeker.concurrency.concurrent_module.AsyncAndCallback.future;

import java.util.concurrent.*;

public class FutureDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<String> future = executorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(3600000);
                return "Hello Arvin";
            }
        });
        System.out.println("waiting...");   //在future.get()之前可执行异步任务
        //一种更节省资源的条件循环等待
        //while(!future.isDone()) {
        //    Thread.yield();
        //}
        String result = future.get();       //同步等待
        System.out.println(result);

        //为何主线程也被阻塞？
    }
}
