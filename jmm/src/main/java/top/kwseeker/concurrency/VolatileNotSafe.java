package top.kwseeker.concurrency;

/**
 * 测试volatile并不能确保线程的安全
 * 结果：
 *  74600 和 200000 差很多
 * 原因：每个线程只能确保取值时是最新的，但是不能确保操作完成后往主内存写的时候，这个值还是原来的值。
 *  比如：线程11取值时（先刷新）获取2748，执行+1操作的几条字节码指令，然后重新写回主内存发现，原来的值已经从2748变成2750了（其他线程改了）
 *  然后线程11还将2749写入到主内存。
 */
public class VolatileNotSafe {

    static volatile int race = 0;
    static void increase() {
        race++;
    }
    private static final int THREADS_COUNT=20;

    public static void main(String[]args) {
        Thread[]threads=new Thread[THREADS_COUNT];
        for(int i=0; i < THREADS_COUNT; i++ ) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 10000; j++) {
                    increase();
                }
            });
            threads[i].start();
        }
        //等待所有累加线程都结束
        //返回当前线程的线程组中活动线程的数量。返回的值只是一个估计值，因为当此方法遍历内部数据结构时，线程数可能会动态更改。
        while(Thread.activeCount() > 2) {       //线程组除了Main线程还有个守护进程
            Thread.currentThread().getThreadGroup().list();
            Thread.yield();
        }

        System.out.println(race);
    }
}
