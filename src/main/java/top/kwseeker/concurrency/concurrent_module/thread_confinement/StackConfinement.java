package top.kwseeker.concurrency.concurrent_module.thread_confinement;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.ThrowsAdvice;

/**
 * 线程封闭
 *      线程封闭是将某些filed只对某个线程可见，然后就不存在并发安全性问题了
 *
 * 线程封闭的方法
 *      1) Ad-hoc
 *      2) 栈封闭 (其实就是局部变量，每个线程维护一份)
 *      3) ThreadLocal (使用ThreadLocal类定义的变量在每个线程中都会维护一份，相互之间不会互相影响)
 */
// 栈封闭
@Slf4j
public class StackConfinement {

    private String globalVar = "A";   //全局变量，所有线程共享

    public void setGlobalVar(String globalVar) {
        this.globalVar = globalVar;
    }

    public String doSomeThing() {
        String threadName = Thread.currentThread().getName();   //局部变量, 每个线程有一份
        log.info("{}, globalVar:{}", threadName, globalVar);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("{}, globalVar:{}", threadName, globalVar);
        return threadName;
    }

    public static void main(String[] args) {
        StackConfinement stackConfinement = new StackConfinement();

        new Thread(() -> {
            stackConfinement.setGlobalVar("B");
            stackConfinement.doSomeThing();
        }).start();

        new Thread(()-> {
            stackConfinement.setGlobalVar("C");
            stackConfinement.doSomeThing();

        }).start();

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
