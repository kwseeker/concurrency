package top.kwseeker.unsafe;

import sun.misc.Unsafe;

public class UnsafeArraySample extends UnsafeGetInstance {

    public static void main(String[] args) {
        Unsafe unsafe = reflectGetUnsafe();
        assert unsafe != null;

        //Class clazz = long[].class;
        Class<?> clazz = String[].class;
        String[] strArray = new String[3];

        //为何首元素的偏移量总是16，前面16字节做什么的？ 存储对象头信息
        long baseAddr = unsafe.arrayBaseOffset(clazz);
        int scale = unsafe.arrayIndexScale(clazz);
        System.out.println("baseAddr=" + baseAddr + ", scale=" + scale);

        //unsafe.putInt(baseAddr, 0, 1);
        //unsafe.putInt(baseAddr, scale, 2);
        //unsafe.putInt(baseAddr, scale * 2, 3);
        //int element = unsafe.getInt(baseAddr, scale * 2);

        //一般情况下没必要这么写，只是演示下可以通过Unsafe像C/C++一样通过偏移量定位数组元素并修改
        unsafe.putObject(strArray, (long) baseAddr, "Hello ");
        unsafe.putObject(strArray, (long) baseAddr + scale, "World ");
        unsafe.putObject(strArray, (long) baseAddr + 2*scale, "!");

        for (String s : strArray) {
            System.out.print(s);
        }
    }
}
