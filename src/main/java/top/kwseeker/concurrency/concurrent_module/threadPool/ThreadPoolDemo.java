package top.kwseeker.concurrency.concurrent_module.threadPool;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Java线程池
 *
 * Java线程池类
 *      java.util.concurrent.Executors
 *      java.util.concurrent.ExecutorService
 *      java.util.concurrent.ThreadPoolExecutor
 *
 * 线程池相对于Thread的优点
 *      1）避免了新建对象，释放对象的资源浪费，性能更好；
 *      2）线程池是对线程的统一管理，可以限制线程数量，避免线程频繁调度，节约系统资源；
 *      3）线程池类相对于Thread提供了一些更强大的功能（如：定时执行、定期执行、并发数控制等）。
 *
 * 使用线程池的步骤：
 *      1）创建线程池
 *          线程池配置
 *              int corePoolSize,       //核心线程数量（最低保存的线程数量，即使没有任务，线程也不会被释放）
 *              int maximumPoolSize,    //最大线程数量（最高保存的线程数量，当线程数量达到这个值，任务阻塞到队列等待空闲线程出现）
 *              long keepAliveTime,     //空闲等待时间（当线程数量超过corePoolSize，且线程空闲时间超过这个值，线程会被释放）
 *              TimeUnit unit,          //空闲等待时间单位（ms, s, m, h, day）
 *              BlockingQueue<Runnable> workQueue,  //任务队列
 *              ThreadFactory threadFactory         //线程工厂
 *
 *              任务队列
 *                  SynchronousQueue
 *                  LinkedBlockingQueue
 *                  DelayedWorkQueue
 *
 *              线程工厂
 *                  defaultThreadFactory
 *
 *              RejectedExecutionHandler
 *                  AbortPolicy
 *
 *          (4+1)种线程池
 *              newCachedThreadPool
 *              newFixedThreadPool
 *                  newSingleThreadPool
 *              newScheduledThreadPool
 *              newWorkStealingPool
 *
 *      2）提交任务
 *          execute()
 *          submit() [相当于execute() + Future()]
 *      3) 线程池监控（监控系统获取ThreadPoolExecutor实例，然后每隔一段时间调用这几个方法查看线程池状态）
 *          getTaskCount() 线程池已执行和未执行任务的数量
 *          getCompletedTaskCount() 已完成的任务数量
 *          getPoolSize() 线程池当前的线程数量
 *          getActiveCount() 当前线程池中正在执行任务的线程数量
 *      4）关闭线程池
 *          shutdown() [会等待未处理完成的任务处理完成]
 *          shutdownNow() [不会等待未处理完成的任务，立即终止]
 *
 *   Executors 使用代理模式, 实际工作的线程池是 ThreadPoolExecutor的几种定制化变种 、
 *      ThreadPoolExecutor的子类ScheduledThreadPoolExecutor 还有 ForkJoinPool 。
 */
@Slf4j
public class ThreadPoolDemo {

    private static void longTimeTask() {
        log.info("longTimeTask start >>>>>>>");
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("<<<<<<< longTimeTask finished");
    }

    public static void main(String[] args) {

        //创建 corePoolSize=0, maximumPoolSize=Integer.MAXVALUE , 空闲超时时间60s, 任务队列为new SynchronousQueue<Runnable>()，
        //线程工厂为默认的Executors.defaultThreadFactory(), RejectedExecutionHandler也默认new AbortPolicy()
        ExecutorService executorServiceCached = Executors.newCachedThreadPool();
        ExecutorService executorServiceCachedFactory = Executors.newCachedThreadPool();

        //固定线程个数的线程池
        //创建 corePoolSize=n, maximumPoolSize=n , 空闲超时时间 0ms, 任务队列为new LinkedBlockingQueue<Runnable>()，
        //线程工厂为默认的Executors.defaultThreadFactory(), RejectedExecutionHandler也默认new AbortPolicy()
        ExecutorService executorServiceFixed = Executors.newFixedThreadPool(9);

        //单线程线程池
        //创建 corePoolSize=1, maximumPoolSize=1 , 空闲超时时间 0ms, 任务队列为new LinkedBlockingQueue<Runnable>()，
        //线程工厂为默认的Executors.defaultThreadFactory(), RejectedExecutionHandler也默认new AbortPolicy()
        ExecutorService executorServiceSingle = Executors.newSingleThreadExecutor();

        //可以执行计划任务的线程池
//        DelayedWorkQueue
        ScheduledExecutorService executorServiceScheduled = Executors.newScheduledThreadPool(1);


        for (int i = 0; i < 10; i++) {
//            final int index = i;
            executorServiceCached.execute(()->{
//                log.info("task: {}", index);
                longTimeTask();
            });

//            executorServiceFixed.execute(()->{
//                log.info("task:{}", index);
//            });

            executorServiceSingle.execute(()->{
//                log.info("task:{}", index);
                longTimeTask();
            });
        }

//        executorServiceScheduled.scheduleAtFixedRate(()->{
//            log.warn("schedule run");
//        }, 1 , 3, TimeUnit.SECONDS);  //延时1s后，每隔3s执行一次
//
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                log.warn("timer run");
//            }
//        }, new Date(), 5*1000);      //定时器，从当前时间开始每隔5s执行一次

        executorServiceCached.shutdown();   //没有这句的话，线程池将一直存在，main线程也不会退出
        //TODO: Thread线程的主线程不会等待其完成，而线程池则会等待，什么原理？
        executorServiceSingle.shutdown();

    }
}
