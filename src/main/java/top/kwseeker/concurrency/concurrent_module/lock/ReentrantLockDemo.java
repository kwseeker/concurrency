package top.kwseeker.concurrency.concurrent_module.lock;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ReentrantLock (可重入锁) 和 Synchronized 的区别
 *
 * 可重入性：
 *      两者都是是可重入的（在一个线程中可以多次获取同一个锁，每获取一次，锁的引用计数加1）
 * 锁的实现：
 *      ReentrantLock基于JDK的CAS, synchronized 基于JVM实现
 * 性能上的区别：
 *      ReentrantLock刚开始性能是由于synchronized, 但是后来synchronized 借鉴ReentrantLock的实现进行了优化，然后性能就相差无几了
 * 功能区别
 *      ReentrantLock 使用上灵活度更高
 *      synchronized使用上更加方便，由编译实现加锁和释放，编代码不容易犯低级错误。
 *
 * ReentrantLock 相对于 synchronized 的独有的功能（当要在代码中实现这些功能的时候，只能使用ReentrantLock，
 *      但是除了这些功能还是推荐使用synchronized, 因为synchronized死锁发生后，JVM可以自动标定，自动释放锁，
 *      使用synchronized外的锁风险还是挺大的，线程容易被各种原因中断、杀死，从而造成某个地方没有成功释放锁）
 *      1） ReentrantLock 可以指定锁是公平锁还是非公平锁（先请求锁的先得到锁，则是公平锁；否则为非公平锁）
 *      2）ReentrantLock 可以通过Condition类分组唤醒需要唤醒的线程（synchronized只能随机唤醒一个或唤醒全部）
 *      3）提供能够中断等待锁的线程机制， lock.lockInterruptibly() [当前线程没有被中断的话就获取锁，线程若已被中断就抛出异常]
 */
// 还是前面讲原子变量的那个例子，之前是使用原子变量解决线程安全问题，这里使用锁的方式解决线程安全问题
@Slf4j
public class ReentrantLockDemo {

    // 请求总数
    public static int clientTotal = 5000;
    // 同时并发执行的线程数
    public static int threadTotal = 200;
    public static int count = 0;

    private final static Lock lock = new ReentrantLock();               //////使用ReentrantLock实现对count的互斥访问

    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        final Semaphore semaphore = new Semaphore(threadTotal);
        final CountDownLatch countDownLatch = new CountDownLatch(clientTotal);
        for (int i = 0; i < clientTotal ; i++) {
            executorService.execute(() -> {
                try {
                    semaphore.acquire();
                    add();
                    semaphore.release();
                } catch (Exception e) {
                    log.error("exception", e);
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        log.info("count:{}", count);
    }

    private static AtomicBoolean flag = new AtomicBoolean(false);
    private static void runOnce() {
        if(!flag.get()) {
            flag.set(true);
            log.info("Product exception");
            int i = 1/0;
        }
    }

    private static void add() {
//        lock.lock();                //加锁，个人感觉加锁放到这里有风险
//        runOnce();
        try {
            lock.lock();            //放在这里更好点，因为放在try外边万一刚加锁还没有进入try块时被中断，是不会执行finally中的unlock()的
                                    //而放在try内部即使被异常中断，也可以成功释放锁
            runOnce();
            count++;
        } finally {
            /**
             * 解锁为什么要放到 finally块中（资源回收也推荐放到finally）
             * The finally Block
             * The finally block always executes when the try block exits. This ensures that the finally block is executed even if an unexpected exception occurs.
             * But finally is useful for more than just exception handling — it allows the programmer to avoid having cleanup code accidentally bypassed by a return,
             * continue, or break. Putting cleanup code in a finally block is always a good practice, even when no exceptions are anticipated.
             * Note: If the JVM exits while the try or catch code is being executed, then the finally block may not execute.
             * Likewise, if the thread executing the try or catch code is interrupted or killed, the finally block may not execute even though the application
             * as a whole continues.
             */
            lock.unlock();          //解锁， 注意在finally{}块中解锁，参考：笔记 《关于 Java 中 finally 语句块的深度辨析》
        }
    }

}
