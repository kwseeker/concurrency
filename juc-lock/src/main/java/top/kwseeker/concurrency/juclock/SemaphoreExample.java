package top.kwseeker.concurrency.juclock;

import java.util.concurrent.Semaphore;

/**
 * 信号量（共享锁）
 *
 * １）调用acquire()方法，如果是非公平模式，内部先tryAcquireShared(1),即CAS尝试获取共享锁，成功的话共享锁计数减１，返回剩余锁计数。
 *
 * ２）如果锁计数>＝0，则说明当前线程成功获取共享锁，当前线程开始处理业务逻辑;　如果锁计数小于0，说明锁不再可被获取，
 * 执行doAcquireSharedInterruptibly()将当前线程放入到Semaphore AQS的等待队列（如果等待队列为空则新建，并使用Node.SHARED作为头节点，
 * 将当前节点放置在头节点后，作为tail节点）中。进入队列后会先自旋，若前驱节点为head节点则再次尝试获取共享锁；否则则LockSupport.park()阻塞等待。
 *
 * ３）当之前获取锁的线程执行完业务释放共享锁，则会增加共享锁可用计数，然后获取头节点，如果头节点后面还有节点，并且节点状态是Node.SIGNAL则LockSupport.unpark()唤醒此节点。
 *
 * ４）之前睡眠的线程被唤醒后继续自旋尝试获取共享锁，这次可以成功获取，获取之后将自己设置为head节点,然后连带着唤醒后面的线程，后面的线程自旋尝试获取锁，自己则去执行自己的业务逻辑。
 */
public class SemaphoreExample {

    /**
     * 举个令牌桶限流的例子
     * @param args
     */
    public static void main(String[] args) {
        Semaphore tokens = new Semaphore(3);
        for (int i = 0; i < 10; i++) {
            new Thread(()-> {
                try {
                    tokens.acquire();
                    System.out.println(Thread.currentThread().getName() + "获取到令牌");
                    System.out.println("执行请求");
                    Thread.sleep(2000);
                    System.out.println(Thread.currentThread().getName() + "释放令牌");
                    tokens.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
