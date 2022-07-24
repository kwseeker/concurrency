package top.kwseeker.unsafe.nio;

import sun.misc.Cleaner;
import sun.misc.Unsafe;
import top.kwseeker.unsafe.UnsafeGetInstance;

import java.io.FileDescriptor;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

public class DirectByteBufferSample {

    public static void main(String[] args) {

    }

    /**
     * DirectByteBuffer Class 是 default 类型，即只有相同package的类可以访问
     *
     * DirectByteBuffer
     *      MappedByteBuffer
     *          ByteBuffer
     *              Buffer
     *              Comparable
     *      DirectBuffer
     * 下面把核心代码抓出来
     */
    public static class MyDirectByteBuffer extends UnsafeGetInstance {
        //Buffer
        //容量(即包含的元素个数)
        private int capacity;
        //第一个不可以被读或写的元素的索引 ？？？
        private int limit;
        //下一个待读取或写入元素的索引，默认0即从头开始读写
        private int position = 0;
        //重置之后position指向的元素的索引
        private int mark = -1;
        //
        long address;

        //ByteBuffer
        //数据主体
        final byte[] hb;
        final int offset;

        //MappedByteBuffer
        private final FileDescriptor fd;

        //分配的堆外内存开始地址
        long base = 0;
        Unsafe unsafe = reflectGetUnsafe();
        //
        private final Cleaner cleaner;
        private final Object att;

        public MyDirectByteBuffer(int cap) {
            this.capacity = cap;
            this.limit = cap;
            this.position = 0;
            this.mark = -1;
            this.hb = null;
            this.offset = 0;
            this.fd = null;

            //分配内存
            base = unsafe.allocateMemory(cap);
            //内存数据清理（全部写0）
            unsafe.setMemory(base, cap, (byte) 0);

            address = base;
            //为当前MyDirectByteBuffer对象指定一个Cleaner(虚引用，双向队列，队列的每个节点包含一个引用队列)
            cleaner = Cleaner.create(this, new Deallocator(base, cap, cap));
            att = null;
        }

        //增
        public MyDirectByteBuffer put(byte x) {
            unsafe.putByte(ix(nextPutIndex()), ((x)));
            return this;
        }

        //改
        public MyDirectByteBuffer put(int i, byte x) {
            unsafe.putByte(ix(checkIndex(i)), ((x)));
            return this;
        }

        //查
        public byte get() {
            return ((unsafe.getByte(ix(nextGetIndex()))));
        }
        public byte get(int i) {
            return ((unsafe.getByte(ix(checkIndex(i)))));
        }

        private long ix(int i) {
            return address + ((long) i);
        }

        final int nextPutIndex() {
            int p = position;
            if (p >= limit) {
                throw  new BufferOverflowException();
            }
            position = p + 1;
            return p;
        }

        final int nextGetIndex() {
            int p = position;
            if (p >= limit)
                throw new BufferUnderflowException();
            position = p + 1;
            return p;
        }

        final int nextGetIndex(int nb) {
            int p = position;
            if (limit - p < nb)
                throw new BufferUnderflowException();
            position = p + nb;
            return p;
        }

        final int checkIndex(int i) {
            if ((i < 0) || (i >= limit))
                throw new IndexOutOfBoundsException();
            return i;
        }
    }

    private static class Deallocator implements Runnable {

        private static Unsafe unsafe = Unsafe.getUnsafe();

        private long address;
        private long size;
        private int capacity;

        private Deallocator(long address, long size, int capacity) {
            assert (address != 0);
            this.address = address;
            this.size = size;
            this.capacity = capacity;
        }

        public void run() {
            if (address == 0) {
                return;
            }
            unsafe.freeMemory(address);
            address = 0;
            //Bits.unreserveMemory(size, capacity);
        }
    }
}
