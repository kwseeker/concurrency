package top.kwseeker.concurrency.juccollection.queue;

import org.junit.Test;
import top.kwseeker.concurrency.juccollection.map.ConcurrentHashMapWrapper;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

/**
 * 调试解析ConcurrentHashMap源码的测试
 */
public class ConcurrentHashMapTest {

    @Test
    public void testConcurrentHashMap() {
        //initialCapacity=0, 实际初始容量是2, 0+(0>>>1)+1 == 1, 不小于1的最小2次幂是1
        //initialCapacity=1, 实际初始容量是4, 1+(1>>>1)+1 == 2, 不小于2的最小2次幂是2
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>(0);

        map.put("A", 1);
        map.put("B", 2);
        map.put("C", 3);
        map.put("D", 4);

        Integer ret = map.get("B");
        assertEquals(ret, new Integer(2));
    }

    @Test
    public void testResize() {
        ConcurrentHashMapWrapper<MyString, Integer> map = new ConcurrentHashMapWrapper<>(32);
        for (int i = 0; i < 26; i++) {
            for (int j = 1; j <= 100; j++) {
                map.put(new MyString((char) ('A' + i) + "" + j), i * 100 + j);
            }
        }

        Integer ret = map.get(new MyString("A11"));
        assertEquals(ret, new Integer(11));
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

    //为测试扩容故意造的一个hashCode冲突很严重的类（很容易出现hashcode相同但是equals为false）
    //hashcode是首字母的也相等
    //Java规范：
    //equals的两个对象，hashcode一定要相等
    //hashcode相等的两个对象，不一定equals
    static class MyString {
        private final String raw;
        private int hash;

        public MyString(String raw) {
            this.raw = raw;
        }

        @Override
        public int hashCode() {
            int h = hash;
            if (h == 0 && raw.length() > 0) {
                h = this.raw.charAt(0);
                hash = h;
            }
            return h;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MyString)) {
                return false;
            }
            return this.raw.equals(((MyString) obj).raw);
        }

        @Override
        public String toString() {
            return this.raw;
        }
    }
}
