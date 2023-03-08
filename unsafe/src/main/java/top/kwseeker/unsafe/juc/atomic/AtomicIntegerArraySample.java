package top.kwseeker.unsafe.juc.atomic;

import sun.misc.Unsafe;
import top.kwseeker.unsafe.UnsafeGetInstance;

public class AtomicIntegerArraySample {

    public static void main(String[] args) {
        MyAtomicIntegerArray array = new MyAtomicIntegerArray(new int[]{1, 1, 1});
        array.getAndAdd(0, 2);
        array.getAndAdd(1, 3);
        array.getAndAdd(2, 4);
        System.out.println(array.get(0));
        System.out.println(array.get(1));
        System.out.println(array.get(2));
    }

    /**
     * AtomicIntegerArray 核心代码
     */
    public static class MyAtomicIntegerArray extends UnsafeGetInstance {
        private static final Unsafe unsafe = reflectGetUnsafe();
        //首元素偏移量，为何基本都是16？存储对象头信息
        private static final int base;
        //1 << shift 是元素偏移量，为何不直接记录元素偏移量，还要搞个转换？
        private static final int shift;
        private final int[] array;

        static {
            assert unsafe != null;
            base = unsafe.arrayBaseOffset(int[].class);
            int scale = unsafe.arrayIndexScale(int[].class);
            if ((scale & (scale - 1)) != 0)
                throw new Error("data type scale not a power of two");
            //转为二进制，从左开始数0的个数
            shift = 31 - Integer.numberOfLeadingZeros(scale);
        }

        public MyAtomicIntegerArray(int[] array) {
            this.array = array;
        }

        public final int getAndAdd(int i, int delta) {
            assert unsafe != null;
            //做了那么多就只是为了获取 offset 用上 compareAndSwapInt(o, offset, v, v + delta)
            return unsafe.getAndAddInt(array, checkedByteOffset(i), delta);
        }

        public final int get(int i) {
            long offset = checkedByteOffset(i);
            assert unsafe != null;
            return unsafe.getIntVolatile(array, offset);
        }

        /**
         * 计算第i个元素的偏移量
         * @param i 第几个元素
         * @return  偏移量
         */
        private long checkedByteOffset(int i) {
            if (i < 0 || i >= array.length)
                throw new IndexOutOfBoundsException("index " + i);
            return byteOffset(i);
        }

        private static long byteOffset(int i) {
            return ((long) i << shift) + base;
        }
    }
}
