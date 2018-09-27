package top.kwseeker.concurrency.concurrent_module.AQSClass;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@Slf4j
public class FutureTaskDemo {

    public static void main(String[] args) {

        FutureTask<String> futureTask = new FutureTask<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                log.info("do something in callable");
                Thread.sleep(5000);
                return "Done";
            }
        });

        new Thread(futureTask).start();
        log.info("do something in main");
        try {
            Thread.sleep(1000);
            String result = futureTask.get();
            log.info("result：{}", result);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
