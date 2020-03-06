package top.kwseeker.concurrency.threadsafety;

import org.junit.Test;

import java.util.Vector;

/**
 * 线程安全类并不是绝对线程安全（不是说使用线程安全的类就不用额外做同步了）
 */
public class ThreadSafeTest {

    private static Vector<Integer> vector = new Vector<>();

    @Test
    public void testVector() {
        while (true) {
            for (int i = 0; i < 10; i++) {
                vector.add(i);
            }
            Thread removeThread = new Thread(() -> {
                for (int i = 0; i < vector.size(); i++) {
                    vector.remove(i);
                }
            });
            Thread printThread = new Thread(() -> {
                for (int i = 0; i < vector.size(); i++) {   //可能这里size()刚读到2，跑到println的时候vector可能已经为空了，这时再get()就会异常
                    System.out.println(vector.get(i));
                }
            });
            removeThread.start();
            printThread.start();
            while(Thread.activeCount() > 20);
        }
    }

}
