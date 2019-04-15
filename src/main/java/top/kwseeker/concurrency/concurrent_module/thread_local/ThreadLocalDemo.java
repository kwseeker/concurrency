package top.kwseeker.concurrency.concurrent_module.thread_local;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.DataFormatException;

/**
 * ThreadLocal 实现原理(之前研究了一遍，不过后来忘光了)
 *
 * 1）实现原理
 *      在某个线程中对ThreadLocal变量做set(value)操作，会首先获取当前线程对象，然后判断当前线程的 threadLocals (ThreadLocalMap，
 *      每个线程都有这个成员变量) 是否为空;
 *      不为空，则直接插入这个值value；
 *
 *          private void set(ThreadLocal<?> key, Object value) {
 *
 *             // We don't use a fast path as with get() because it is at
 *             // least as common to use set() to create new entries as
 *             // it is to replace existing ones, in which case, a fast
 *             // path would fail more often than not.
 *
 *             Entry[] tab = table;
 *             int len = tab.length;
 *             int i = key.threadLocalHashCode & (len-1);
 *
 *             for (Entry e = tab[i];
 *                  e != null;
 *                  e = tab[i = nextIndex(i, len)]) {
 *                 ThreadLocal<?> k = e.get();
 *
 *                 if (k == key) {
 *                     e.value = value;
 *                     return;
 *                 }
 *
 *                 if (k == null) {
 *                     replaceStaleEntry(key, value, i);
 *                     return;
 *                 }
 *             }
 *
 *             tab[i] = new Entry(key, value);
 *             int sz = ++size;
 *             if (!cleanSomeSlots(i, sz) && sz >= threshold)
 *                 rehash();
 *         }
 *
 *      为空，则新建一个 ThreadLocalMap，并设置第一个值为value；
 *
 *          ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue) {
 *             table = new Entry[INITIAL_CAPACITY];
 *             int i = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1);
 *             table[i] = new Entry(firstKey, firstValue);
 *             size = 1;
 *             setThreshold(INITIAL_CAPACITY);
 *         }
 *
 *      ThreadLocalMap 内部是一个哈希表（即通过Hash索引的数组，默认容量16，使用当前 ThreadLocal对象的 threadLocalHashCode & (INITIAL_CAPACITY -1) 作为索引 ），
 *      每一个成员 Entry 都是一个包含了值的 ThreadLocal 的弱引用。
 *
 *      InheritableThreadLocal类是ThreadLocal的子类。InheritableThreadLocal允许一个线程创建的所有子线程访问其父线程的值。
 *
 *      Ps：
 *      JDK8之前静态变量存储在方法区(方法区是JVM的规范，永久代是方法区的具体实现),JDK8之后就取消了“永久代”，取而代之的是“元空间”，
 *      永久代中的数据也进行了迁移，静态成员变量迁移到了堆中。
 *
 * 2）使用demo
 *      只要是某个线程中使用到ThreadLocal类型变量，这个线程就会为这个变量分配单独的内存。
 *
 * 3）内存泄漏隐患与内存泄漏检测
 *
 */
public class ThreadLocalDemo {

    public static class Task implements Runnable {
        //线程首次调用ThreadLocal变量的get方法时调用initialValue()
//        private static final ThreadLocal<Integer> counter = new ThreadLocal<Integer>() {              //测试完成任务计数
        private ThreadLocal<Integer> counter = new ThreadLocal<Integer>() {                             //测试ThreadLocalMap碰撞后的处理与扩容策略
            protected Integer initialValue() {         //initialValue() 是为了防止get的时候value值为空
                return 0;
            }
        };

        @Override
        public void run() {
            try {
                int count = counter.get();
                counter.set(count+1);
                System.out.println(Thread.currentThread().getName() + "is running");
                Thread.sleep(100);
                //执行完后要手动释放不然会造成内存泄漏
                counter.remove();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Profiler {

        private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();
        private static final ThreadLocal<Long> END_TIME = new ThreadLocal<>();

        public static void begin() {
            START_TIME.set(System.currentTimeMillis());
        }

        public static void end() {
            END_TIME.set(System.currentTimeMillis());
        }

        public static Long costTime() throws NullPointerException {
            Long startTime = START_TIME.get();
            Long endTime = END_TIME.get();
            if(startTime == null || endTime == null) {
                throw new NullPointerException("开始时间以及结束时间不能为空");
            }
            return END_TIME.get() - START_TIME.get();
        }
    }

    public static void main(String[] args) {

//        AtomicInteger nextHashCode = new AtomicInteger();     //值默认为0
//        final int HASH_INCREMENT = 0x61c88647;                //1640531527
//        int nextHC = nextHashCode.getAndAdd(HASH_INCREMENT);  //获取当前值并加上一个值然后写回去
//        System.out.println(nextHC);

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 40; i++) {
            executorService.execute(new Task());
        }

        Thread thread1 = new Thread(() -> {
            try {
                Profiler.begin();
                Thread.sleep(2000);
                Profiler.end();
                System.out.println(Profiler.costTime());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Thread thread2 = new Thread(() -> {
            try {
                Profiler.begin();
                Thread.sleep(1000);
                Profiler.end();
                System.out.println(Profiler.costTime());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
            executorService.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
