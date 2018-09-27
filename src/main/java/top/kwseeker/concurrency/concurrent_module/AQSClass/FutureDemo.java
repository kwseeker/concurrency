package top.kwseeker.concurrency.concurrent_module.AQSClass;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * Callable 接口相对于 Runnable 区别是有返回值且任务无法执行的话会抛出异常
 *      V call() throws Exception;
 *      public abstract void run();
 *
 * Future<V> 也是个接口
 *      用于获取任务执行结果，可以撤销任务，查看任务状态，以及设置等待时间
 *
 * TODO:
 */
@Slf4j
public class FutureDemo {

    static class MyCallable implements Callable<String> {
        @Override
        public String call() throws Exception {
            log.info("do something in callable");
            Thread.sleep(3000);
            return "Done";
        }
    }

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        Future<String> future = executorService.submit(new MyCallable());   //submit() 带有Future<T> 返回值

        log.info("do something in main");
        try {
            Thread.sleep(1000);
            String result = future.get();   //无限期等待获取结果
//            String result = future.get(1000, TimeUnit.MILLISECONDS);    //设置了deadline, 任务还是没完成，是会抛出 TimeoutException 的
            log.info("result:{}", result);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
