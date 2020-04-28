package top.kwseeker.concurrency.juccollection.copyonwrite;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 本以为是基于`ReentrantReadWriteLock`实现，但是实际不是的，而是采取“只对写操作加`ReentrantLock`,
 * 写过程中会新复制一个容器, 将新元素加进去之后，再将新的容器赋值给引用对象”。
 *
 * CopyOnWriteArrayList只对写操作加锁，写过程中先深复制一个`array`，然后将元素添加到新的`array`，再将引用指向这个新的`array`，读操作直接从array通过索引读取。
 */
public class CopyOnWriteArrayListExample {

    public static void main(String[] args) {
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
        list.add("a");
        System.out.println(list.get(0));
        list.add("b");
        System.out.println(list.get(1));
    }
}
