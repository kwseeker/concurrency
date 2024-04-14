package top.kwseeker.concurrency.jmm.feature;

import org.junit.Test;

/**
 * volatile无法保证原子性测试
 * 比如有两个线程 A、B, 间隔代表两者时间差
 * A: 读counter到工作内存（０）　            -> 执行加１             -> 写回主内存（１）
 * B:                 读counter到工作内存（０）　　     -> 执行加１　　              -> 写回主内存（１）
 * 这种B已经执行了加１，A才更新counter, B并不会把这次加１操作作废掉再重新读取
 */
public class VolatileCannotEnsureAtomicTest {

    private volatile static int counter = 0;

    @Test
    public void test() {
        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    counter++;
                }
            }).start();
        }
        System.out.println(counter);
    }
}
