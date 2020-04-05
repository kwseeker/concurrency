package top.kwseeker.concurrency.juclock;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * CyclicBarrier原理
 * 如果让自己结合已有技术手段可以怎么做？如何实现多个线程从同一起跑线起跑？
 * notifyAll()?是否能够实时唤醒？
 * 和猜想的差不多，同样是等待唤醒机制，只不过是使用的ReentrantLock的Condition条件等待和唤醒。
 */
public class CyclicBarrierAnalysis {

    public static void main(String[] args) {
        /**
         * parties:　参与同步起跑的线程数量，只有当这么多线程准备好（调用await()）才会触发起跑
         * count:　还未准备就绪的线程数量
         * barrierCommand:　触发起跑时执行的命令
         */
        CyclicBarrier barrier = new CyclicBarrier(2);
        new Thread(()->{
            try {
                //await()里面在等待一个计数变为０（起跑信号），每有一个线程准备就绪（即await()）计数就减１(这个过程用ReentrantLock保护计数等值的线程安全)；
                //前面准备好的线程会进入条件等待（释放锁）；
                //最后一个线程进来时count计数变为０，触发条件唤醒signalAll()；甚至重置计数和generation（即还可以进行下一轮的"比赛"）。
                barrier.await();
                for (int i = 0; i < 20; i++) {
                    System.out.println("A:"+i);
                }
                //等待开启下一轮同步起跑
                barrier.await();
                for (int i = 20 ;i < 40; i++) {
                    System.out.println("next A: " + i);
                }
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(()->{
            try {
                barrier.await();
                for (int i = 0; i < 20; i++) {
                    System.out.println("B:"+i);
                }
                barrier.await();
                for (int i = 20 ;i < 40; i++) {
                    System.out.println("next B: " + i);
                }
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }).start();
    }

}

/**
 * CyclicBarrier两个线程测试结果：
 * 多个线程可以在几条for循环的时间差中做出反映，ns级别，同步起跑效果已经很好了
 * B:0
 * B:1
 * B:2
 * B:3
 * B:4
 * B:5
 * B:6
 * B:7
 * B:8
 * A:0
 * B:9
 * B:10
 * B:11
 * B:12
 * B:13
 * A:1
 * A:2
 * A:3
 * A:4
 * A:5
 * A:6
 * A:7
 * A:8
 * A:9
 * A:10
 * A:11
 * A:12
 * A:13
 * A:14
 * A:15
 * A:16
 * A:17
 * B:14
 * A:18
 * A:19
 * B:15
 * B:16
 * B:17
 * B:18
 * B:19
 * next B: 20
 * next A: 20
 * next A: 21
 * next A: 22
 * next A: 23
 * next A: 24
 * next A: 25
 * next A: 26
 * next A: 27
 * next A: 28
 * next A: 29
 * next A: 30
 * next A: 31
 * next A: 32
 * next A: 33
 * next A: 34
 * next A: 35
 * next A: 36
 * next A: 37
 * next A: 38
 * next A: 39
 * next B: 21
 * next B: 22
 * next B: 23
 * next B: 24
 * next B: 25
 * next B: 26
 * next B: 27
 * next B: 28
 * next B: 29
 * next B: 30
 * next B: 31
 * next B: 32
 * next B: 33
 * next B: 34
 * next B: 35
 * next B: 36
 * next B: 37
 * next B: 38
 * next B: 39
 */
