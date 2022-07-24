package top.kwseeker.unsafe;

import sun.misc.Unsafe;

public class UnsafeMemorySample extends UnsafeGetInstance {

    public static void main(String[] args) {
        Unsafe unsafe = reflectGetUnsafe();
        assert unsafe != null;
        //内存分配,返回地址
        long memAddr = unsafe.allocateMemory(4);
        System.out.println("Allocate 4 bytes memory, address = " + memAddr);
        //内存赋值
        unsafe.putInt(memAddr, 99);
        //读取内存
        System.out.println("Final get value: " + unsafe.getInt(memAddr));

        unsafe.freeMemory(memAddr);
    }
}
