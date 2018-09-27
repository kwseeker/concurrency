package top.kwseeker.concurrency.concurrent_module.lock;

import java.util.concurrent.locks.StampedLock;

/**
 * StampedLock 也是读写锁，同时是乐观锁
 *
 * 控制锁的方式：
 *      读：
 *      写：
 *      乐观读：
 *
 */
// StampedLock 源码作者给的一个 Demo
// TODO: 代码有点难以理解，需要继续研究源码实现
public class StampedLockDemo {

    class Point {
        private double x, y;
        private final StampedLock sl = new StampedLock();

        void move(double deltaX, double deltaY) { // an exclusively locked method
            long stamp = sl.writeLock();
            try {
                x += deltaX;
                y += deltaY;
            } finally {
                sl.unlockWrite(stamp);
            }
        }

        //下面看看乐观读锁案例
        double distanceFromOrigin() { // A read-only method
            long stamp = sl.tryOptimisticRead();    //获得一个乐观读锁
            double currentX = x, currentY = y;      //将两个字段读入本地局部变量
            if (!sl.validate(stamp)) {      //检查发出乐观读锁后同时是否有其他写锁发生？如果没有写锁，乐观读的字段有效；否则需要补救
                stamp = sl.readLock();      //乐观读时有写锁发生，加悲观读锁重新读取
                try {
                    currentX = x; // 将两个字段读入本地局部变量
                    currentY = y; // 将两个字段读入本地局部变量
                } finally {
                    sl.unlockRead(stamp);
                }
            }
            return Math.sqrt(currentX * currentX + currentY * currentY);
        }

        //下面是悲观读锁案例
        void moveIfAtOrigin(double newX, double newY) { // upgrade
            // Could instead start with optimistic, not read mode
            long stamp = sl.readLock();
            try {
                while (x == 0.0 && y == 0.0) { //循环，检查当前状态是否符合
                    long ws = sl.tryConvertToWriteLock(stamp); //将读锁转为写锁
                    if (ws != 0L) { //这是确认转为写锁是否成功
                        stamp = ws; //如果成功 替换票据
                        x = newX; //进行状态改变
                        y = newY;  //进行状态改变
                        break;
                    } else { //如果不能成功转换为写锁
                        sl.unlockRead(stamp);  //我们显式释放读锁
                        stamp = sl.writeLock();  //显式直接进行写锁 然后再通过循环再试
                    }
                }
            } finally {
                sl.unlock(stamp); //释放读锁或写锁
            }
        }
    }
}
