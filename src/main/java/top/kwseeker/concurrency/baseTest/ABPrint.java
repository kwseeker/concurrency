package top.kwseeker.concurrency.baseTest;

import java.util.HashMap;

/**
 * 使用对象的 wait() notify(), 首先需要获取对象的同步锁。
 * 否则会抛出java.lang.IllegalMonitorStateException异常，
 *      Thrown to indicate that a thread has attempted to wait on an
 *      object's monitor or to notify other threads waiting on an object's
 *      monitor without owning the specified monitor.
 */
public class ABPrint {

    public static void main(String[] args) {

        ThreadGroup threadGroup = new ThreadGroup("MyThreadGroup");
        HashMap<String, Thread> threadHashMap = new HashMap<>();

        Thread threadA = new Thread(threadGroup, () -> {
            try {
                for(;;) {
                    Thread.sleep(1000);
                    System.out.print("A");
                    //notify threadB
                    synchronized (threadHashMap.get("threadB")) {
                        threadHashMap.get("threadB").notify();
                    }
                    //wait to notified by threadB
//                Thread.currentThread().wait();
                    synchronized (threadHashMap.get("threadA")) {
                        threadHashMap.get("threadA").wait();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "threadA");
        threadHashMap.put("threadA", threadA);

        Thread threadB = new Thread(threadGroup, () -> {
            try {
                for(;;) {
                    Thread.sleep(1000);
                    System.out.print("B");
                    //notify threadA
                    synchronized (threadHashMap.get("threadA")) {
                        threadHashMap.get("threadA").notify();
                    }
                    //wait to notified by threadA
//                Thread.currentThread().wait();
                    synchronized (threadHashMap.get("threadB")) {
                        threadHashMap.get("threadB").wait();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "threadB");
        threadHashMap.put("threadB", threadB);

        threadA.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        threadB.start();
    }
}
