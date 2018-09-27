package top.kwseeker.concurrency.concurrent_module.AQSClass;

import lombok.extern.slf4j.Slf4j;
import org.omg.PortableServer.ThreadPolicy;

import java.util.Queue;
import java.util.concurrent.*;

/**
 * CountDownLatch
 *
 * 使用场景：
 *      有依赖关系的线程场景，比如线程A的执行依赖于线程B,就可以使用CountDownLatch锁住A并设初始值为1,
 *      当线程B执行完成调用countDown(),这时A线程可以继续执行。
 *      （比如：将一个大的任务分成若干小任务，当小任务全部执行完，再进行汇总操作）
 */
@Slf4j
public class CountDownLatchDemo {

    //模拟自动泡茶机泡茶的任务：清洗茶具A，烧开水B，将茶叶放到茶杯C，倒开水D
    //依赖关系 B依赖A C依赖A D依赖A/B
    private CountDownLatch countDownLatchB = new CountDownLatch(1);
    private CountDownLatch countDownLatchC = new CountDownLatch(1);
    private CountDownLatch countDownLatchD = new CountDownLatch(2);
    private CountDownLatch countDownLatchFinish = new CountDownLatch(1);

    // A
    public void washTeaSet() {
        try {
            // wash tea set
            log.info("Wash tea set ...");
            Thread.sleep(5000);
            countDownLatchB.countDown();
            countDownLatchC.countDown();
        } catch (InterruptedException e) {
            log.error("{}", e);
        }
    }
    // B
    public void boilWater() {
        try {
            countDownLatchB.await();
            // boil water
            log.info("Boil water ...");
            Thread.sleep(15000);
            countDownLatchD.countDown();
        } catch (InterruptedException e) {
            log.error("{}", e);
        }
    }
    // C
    public void putTeaIntoCup() {
        try {
            countDownLatchC.await();
            // put tea into cup
            log.info("put tea into cup ...");
            Thread.sleep(2000);
            countDownLatchD.countDown();
        } catch (InterruptedException e) {
            log.error("{}", e);
        }
    }
    // D
    public void putWaterIntoCup() {
        try {
            countDownLatchD.await();
            // put water into cup
            log.info("put water into cup ...");
            Thread.sleep(1000);
            countDownLatchFinish.countDown();
        } catch (InterruptedException e) {
            log.error("{}", e);
        }
    }

    public CountDownLatch getCountDownLatchFinish() {
        return countDownLatchFinish;
    }

    public static void main(String[] args) {
//        ExecutorService executorService = Executors.newCachedThreadPool();
        ThreadPoolExecutor executorService = new ThreadPoolExecutor(0, 8,
                60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
        CountDownLatchDemo makeTea = new CountDownLatchDemo();

        executorService.execute(() -> {
            makeTea.boilWater();
        });
        executorService.execute(() -> {
            makeTea.putTeaIntoCup();
        });
        executorService.execute(() -> {
            makeTea.putWaterIntoCup();
        });
        executorService.execute(() -> {
            makeTea.washTeaSet();
        });

        try {
            makeTea.getCountDownLatchFinish().await(25, TimeUnit.SECONDS);
            if(executorService.getActiveCount() > 0) {
                log.info("Oh, I can not wait! Check, please!");
            } else if (executorService.getActiveCount() == 0) {
                log.info("Wow, hot tea is coming!");
            } else {
                throw new Exception("Wrong task number!");
            }
        } catch (Exception e) {
            log.error("{}", e);
        } finally {
            executorService.shutdownNow();
        }
    }
}
