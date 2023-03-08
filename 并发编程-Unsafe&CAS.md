# Unsafe & CAS 机制

Unsafe是位于`sun.misc`包下的一个类，主要提供一些用于执行低级别、不安全操作的方法，如直接访问系统内存资源、自主管理内存资源等，这些方法在提升Java运行效率、增强Java语言底层资源操作能力方面起到了很大的作用。但由于Unsafe类使Java语言拥有了类似C语言指针一样操作内存空间的能力，这无疑也增加了程序发生相关指针问题的风险。在程序中过度、不正确使用Unsafe类会使得程序出错的概率变大，使得Java这种安全的语言变得不再“安全”，因此对Unsafe的使用一定要慎重。

要在企业项目中使用Unsafe, 需要对其有十足的理解，否则就不要用。

## Unsafe功能

+ **内存操作**
+ **CAS**
+ **Class及静态成员操作**
+ **对象操作**
+ **数组操作**
+ **内存屏障**
+ **系统操作**
+ **线程调度**

## CAS应用与原理

### 应用

通过`CAS`实现线程安全的操作，可以参考`JDK`源码的原子类实现，Unsafe在`JDK`源码中有非常多的使用(如：原子类、`ForkJoinPool`、...)。

**1) 获取Unsafe单例对象**（需要反射获取单例对象[绕过调用类的类加载器检查]或者通过`BootstrapClassLoader`加载，否则会报`SecurityException`异常）；

**2) 获取要操作的对象的成员变量在对象中的偏移量**(JVM内存地址偏移)；

```java
public static long getFieldOffset(Class<?> clazz, String fieldName) {
    try {
        Field field = clazz.getDeclaredField(fieldName);
        return getUnsafe().objectFieldOffset(field);
    } catch (Exception e) {
        throw new Error(e);
    }
}
```

**3) 在自旋中调用`compareAndSwapXxx()`**（自旋是为了失败重试）。

参考：`unsafe` 模块 `UnsafeCASTest.java`，`juc-atomic`模块 `UnsafeOperator`。

### 原理

记得之前看`JVM`的时候有总结过。



## 附录

### 参考

[Java魔法类：Unsafe应用解析](https://tech.meituan.com/2019/02/14/talk-about-java-magic-class-unsafe.html)

[Java Magic. Part 4: sun.misc.Unsafe](http://mishadoff.com/blog/java-magic-part-4-sun-dot-misc-dot-unsafe/)

深入理解Java虚拟机（第2版）