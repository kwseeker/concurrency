package top.kwseeker.concurrency.thread;

import org.junit.Test;

/**
 * Java线程间并没有严格的父子关系
 * 无法直接在子线程中获取父线程信息
 */
public class ThreadGroupTest {

    @Test
    public void testGetParentThreadId() throws InterruptedException {
        Thread parentThread = Thread.currentThread();
        long id = parentThread.getId();
        System.out.println("parent thread id:" + id);

        Thread childThread = new Thread(() -> {
            Thread currentThread = Thread.currentThread();
            //线程默认是属于创建它的线程的分组
            ThreadGroup parentGroup = currentThread.getThreadGroup().getParent();
            //如果创建线程使用的默认分组，则可以通过线程分组获取分组中第一个线程，即当前线程的父线程
            //不过JDK并没有提供获取线程的公共方法
        }, "thread-son");

        childThread.start();
        childThread.join();
    }
}
