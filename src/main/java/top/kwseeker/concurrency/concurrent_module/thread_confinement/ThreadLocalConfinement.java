package top.kwseeker.concurrency.concurrent_module.thread_confinement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadLocalConfinement {

//    private String globalVar = "A";   //全局变量，所有线程共享
    private ThreadLocal<String> threadLocalVar = new ThreadLocal<>();   //ThreadLocal 全局变量， 每个线程维护一份

    public ThreadLocal<String> getThreadLocalVar() {
        return threadLocalVar;
    }

    public String doSomeThing() {
        String threadName = Thread.currentThread().getName();   //局部变量, 每个线程有一份
        log.info("{}, threadLocalVar:{}", threadName, threadLocalVar.get());
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("{}, threadLocalVar:{}", threadName, threadLocalVar.get());
        return threadName;
    }

    public static void main(String[] args) {
        ThreadLocalConfinement threadLocalConfinement = new ThreadLocalConfinement();

        new Thread(() -> {
            threadLocalConfinement.getThreadLocalVar().set("B");
            threadLocalConfinement.doSomeThing();
        }).start();

        new Thread(()-> {
            threadLocalConfinement.getThreadLocalVar().set("C");
            threadLocalConfinement.doSomeThing();

        }).start();

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
//结果：程序启动之初，两个线程对ThreadLocal分别写一个不同的值，100ms后两个线程重新检测这个值，
// 使用 ThreadLocal会发现值不同说明ThreadLocal全局变量不是线程共享的。
//01:42:48.701 [Thread-1] INFO top.kwseeker.concurrency.concurrent_module.thread_confinement.ThreadLocalConfinement - Thread-1, threadLocalVar:C
//01:42:48.701 [Thread-0] INFO top.kwseeker.concurrency.concurrent_module.thread_confinement.ThreadLocalConfinement - Thread-0, threadLocalVar:B
//01:42:48.807 [Thread-0] INFO top.kwseeker.concurrency.concurrent_module.thread_confinement.ThreadLocalConfinement - Thread-0, threadLocalVar:B
//01:42:48.807 [Thread-1] INFO top.kwseeker.concurrency.concurrent_module.thread_confinement.ThreadLocalConfinement - Thread-1, threadLocalVar:C