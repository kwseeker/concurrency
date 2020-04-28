package top.kwseeker.concurrency.thread.state;

import org.junit.Test;

import java.util.concurrent.locks.LockSupport;

/**
 //Java线程状态切换
 public static enum State {
     //新建
     NEW,

     //运行，包含两种情况：可执行，正在执行
     //NEW->RUNNABLE: start(), 可能为Ready也可能为Running
     //Ready->Running: 系统调度
     //Running->Ready: Thread.yield()
     RUNNABLE,

     //阻塞
     //RUNNABLE->BLOCKED: 进入锁代码块，还未获取锁,
     //               　　suspend()基于锁实现也会导致java线程进入阻塞状态。suspend()由于可能导致死锁已经被弃用。
     //BLOCKED->RUNNABLE: 获取锁
     BLOCKED,

     //无限等待
     //RUNNABLE->WAITING: wait(), join(), LockSupport.park()
     //WAITING->RUNNABLE: notify(), notifyAll(), LockSupport.unpark()
     WAITING,

     //超时等待
     //RUNNABLE->TIMED_WAITING: sleep(long), wait(long), join(long), LockSupport.parkNanos(), LockSupport.parkUntil()
     //TIMED_WAITING->RUNNABLE: notify(), notifyAll(), LockSupport.unpark()
     TIMED_WAITING,

     //终止：执行完成后终止
     TERMINATED;
 }*/
public class ThreadStateTest {

    //NEW
    //NEW->RUNNABLE
    @Test
    public void test1() {
        Thread thread = new Thread(() -> {
            while(true){}
        });
        System.out.println(thread.getState());      //NEW
        thread.start();
        System.out.println(thread.getState());      //RUNNABLE
    }


    static void runningWork(long delay) {
        long currentTime = System.currentTimeMillis();
        long beginTime = currentTime;
        while (currentTime < beginTime + delay) {
            Thread.yield();     //RUNNING->READY
            currentTime = System.currentTimeMillis();
        }
    }
    synchronized private void doWork() {
        System.out.println("thread begin: " + System.currentTimeMillis());
        long currentTime = System.currentTimeMillis();
        long beginTime = currentTime;
        while (currentTime < beginTime + 5000) {
            Thread.yield();     //RUNNING->READY
            currentTime = System.currentTimeMillis();
        }
        System.out.println("thread done: " + System.currentTimeMillis());
    }
    //RUNNABLE->BLOCKED
    //BLOCKED->RUNNABLE
    @Test
    public void test2() throws InterruptedException {
        Thread thread1 = new Thread(() -> {
            runningWork(0);
            doWork();
        });    //thread1前10秒获取锁
        Thread thread2 = new Thread(() -> {
            runningWork(2000);
            doWork();
        });    //thread2后10秒获取锁
        thread1.start();
        thread2.start();
        Thread.sleep(1000);
        System.out.println(thread2.getState());     //RUNNABLE
        Thread.sleep(2000);
        System.out.println(thread2.getState());     //BLOCKED
        Thread.sleep(3000);
        System.out.println(thread2.getState());     //RUNNABLE
        Thread.sleep(5000);
        System.out.println(thread2.getState());     //TERMINATED
    }


    public static class Work implements Runnable {
        private final Object object;
        Work(Object object) {
            this.object = object;
        }
        @Override
        public void run() {
            try {
                runningWork(2000);

                //Thread.sleep(3000);           //TIMED_WAITING

                //synchronized (object) {
                //    System.out.println("I go to wait");
                //    object.wait(3000);      //TIMED_WAITING
                //    System.out.println("I'm awake");
                //}

                ((Thread)object).join(3000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public static class Work1 implements Runnable {
        private final Object object;
        Work1(Object object) {
            this.object = object;
        }
        @Override
        public void run() {
            runningWork(3500);
            //synchronized (object) {
            //    object.notify();
            //}
        }
    }
    //RUNNABLE->WAITING/TIMED_WAITING
    //WAITING->RUNNABLE
    //主线程查询子线程状态
    @Test
    public void test3() throws InterruptedException {
        Object obj = new Object();
        Thread thread2 = new Thread(new Work1(obj));
        thread2.start();
        //Thread thread1 = new Thread(new Work(obj));
        Thread thread1 = new Thread(new Work(thread2));
        thread1.start();

        Thread.sleep(1000);
        System.out.println(thread1.getState());     //RUNNABLE
        Thread.sleep(2000);
        System.out.println(thread1.getState());     //WAITING
        Thread.sleep(1000);
        System.out.println(thread1.getState());     //WAITING
    }

    @Test
    public void test4() throws InterruptedException {
        Thread thread1 = new Thread(()->{
            runningWork(2000);
            //LockSupport.park();
            long beginTime = System.currentTimeMillis();
            LockSupport.parkUntil(beginTime + 2000);
        });
        thread1.start();
        Thread.sleep(1000);
        System.out.println(thread1.getState());     //RUNNABLE
        Thread.sleep(2000);
        System.out.println(thread1.getState());     //WAITING
    }

}
