package top.kwseeker.unsafe.nio;

import org.junit.Test;
import sun.misc.Cleaner;

/**
 * 虚引用测试
 */
public class PhantomReferenceTest {

    @Test
    public void testPhantomReference() {
        Integer value = new Integer(5);
        //为 value 对象
        Cleaner.create(value, () -> {
            System.out.println("执行清理");
        });
        value = null;
        System.gc();
    }
}
