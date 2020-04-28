package top.kwseeker.concurrency.juclock;

/**
 * ReentrantLock Condition 比　synchronized wait/notify　更强大
 * １）可以有多种条件, 参考LinkedBlockingQueue的实现
 * ２）可以选择性地通知,但是这里的选择通知，并不是说可以针对某个等待的线程进行通知，而是可以选择通知监听哪个条件的那部分线程。
 */
public class ReentrantLockConditionExample {

    public static void main(String[] args) {

    }
}
