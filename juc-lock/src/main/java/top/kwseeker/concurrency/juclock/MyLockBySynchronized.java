package top.kwseeker.concurrency.juclock;

/**
 * 使用任何一种同步器可以实现其他同步器
 * 测试：
 * 1）使用synchronized实现自己的互斥锁
 */
public class MyLockBySynchronized {

    private boolean locked = false;
    //private Object locker = new Object();

    //获取锁：只有一个线程可以获得互斥量，其余线程都得等待（可以使线程等待的方法：wait()，join()，LockSupport.park()）
    //但是这个场景除了wait(),另两种方式都不合适。
    public void lock() {
        synchronized (this) {
            try {
                //1)检查状态，是否锁定
                while(locked) {
                    //2)已经锁定，则等待，但是注意需要重新判断一下locked
                    this.wait();
                }
                //if(locked) {
                //    this.wait();      //TODO：notify()唤醒任意一个的等待的线程，按理说这么写应该是可以的啊？
                //}
                //3)未锁定或者其他线程已经释放锁定，则本线程设置状态为锁定状态
                locked = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void unlock() {
        synchronized (this) {
            //4)修改锁定状态为未锁定
            locked = false;
            //5)唤醒等待获取锁的线程
            //this.notify();
            this.notifyAll();
        }
    }
}
