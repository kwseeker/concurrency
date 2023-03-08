package top.kwseeker.unsafe.cas.lock;

import org.junit.Assert;
import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class UnsafeCASTest {

    //volatile int total = 0;
    int total = 0;

    @Test
    public void testConcurrentModify() throws InterruptedException {
        //int total = 0;
        CountDownLatch latch = new CountDownLatch(5);
        ExecutorService es = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5; i++) {
            es.submit(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (int j = 0; j < 10000; j++) {
                    total++;
                }
                System.out.println(Thread.currentThread().getName() + " done");
            });
            latch.countDown();
        }
        es.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println(total);
        Assert.assertNotEquals(50000, total);
    }

    @Test
    public void testCASConcurrentModify() throws NoSuchFieldException, InterruptedException, ClassNotFoundException, IllegalAccessException {
        // 直接使用Unsafe类会报“java.lang.SecurityException: Unsafe”异常, 本来这个类只是服务于Java核心类库的
        //sun.misc.Unsafe U = sun.misc.Unsafe.getUnsafe();
        // 通过反射
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        Unsafe U = (Unsafe) field.get(null);

        long TOTAL = U.objectFieldOffset(UnsafeCASTest.class.getDeclaredField("total"));
        CountDownLatch latch = new CountDownLatch(5);
        ExecutorService es = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5; i++) {
            es.submit(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (int j = 0; j < 100000; j++) {
                    for(;;) {
                        if (U.compareAndSwapInt(this, TOTAL, total, total+1)) {     //注意此方法内部没有自旋，失败后直接返回boolean结果
                            break;  //跳出自旋
                        }
                    }
                }
                System.out.println(Thread.currentThread().getName() + " done");
            });
            latch.countDown();
        }
        es.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println(total);
        Assert.assertEquals(500000, total);
    }
}
