package top.kwseeker.concurrency.juccollection.queue;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

/**
 * 调试解析ConcurrentHashMap源码的测试
 */
public class ConcurrentHashMapTest {

    private ConcurrentHashMap<String, Integer> map;

    @Before
    public void init() {
        //initialCapacity=0, 实际初始容量是2, 0+(0>>>1)+1 == 1, 不小于1的最小2次幂是1
        //initialCapacity=1, 实际初始容量是4, 1+(1>>>1)+1 == 2, 不小于2的最小2次幂是2
        map = new ConcurrentHashMap<>(0);
    }

    @Test
    public void testConcurrentHashMap() {
        map.put("A", 1);
        map.put("B", 2);
        map.put("C", 3);
        map.put("D", 4);

    }

    @Test
    public void testResizeStamp() {
        int RESIZE_STAMP_BITS = 16;
        int RESIZE_STAMP_SHIFT = 16;
        int rs = Integer.numberOfLeadingZeros(1) | (1 << (RESIZE_STAMP_BITS - 1));
        assertEquals(32799, rs);
        //10000000 00011111 00000000 00000010
        System.out.println(Integer.toBinaryString((rs << RESIZE_STAMP_SHIFT) + 2));
    }
}
