package top.kwseeker.concurrency.liveness;

import org.junit.Test;

/**
 * 活跃度问题　之　顺序死锁
 * 顺序死锁解决：按固定的顺序请求锁
 * !!! 下面的实例，第一反应是可以通过账户用户ID大小比较排序,确定请求锁的顺序（引入了业务依赖），但是书上提供了更好的方法 System.identityHashCode() 明显更通用也没有业务逻辑污染。
 * System.identityHashCode(Object) 如果有覆写hashCode()则使用覆写的hashCode()计算hash值，否则使用默认的hashCode(), Object为null的话返回０
 */
public class OrderingDealLockTest {

    @Test
    public void testTransferMoney() throws InterruptedException {
        Account arvin = new Account(100L, 300);
        Account nancy = new Account(101L, 200);

        Thread thread1 = new Thread(() -> {
            try {
                transferMoney(arvin, nancy, 30);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        Thread thread2 = new Thread(() -> {
            try {
                transferMoney(nancy, arvin, 20);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread1.start();
        thread2.start();

        thread1.join(10000);
        thread1.join(10000);

        System.out.printf("arvin balance=%d, nancy balance=%d", arvin.getBalance(), nancy.getBalance());
    }

    private void transferMoney(Account from, Account to, int amount) throws Exception {
        Account firstLock, nextLock;
        if (System.identityHashCode(from) < System.identityHashCode(to)) {
            firstLock = from;
            nextLock = to;
        } else {
            firstLock = to;
            nextLock = from;
        }
        //万一identityHashCode() hash冲突呢？可能再需要加些其他条件。

        synchronized (firstLock) {
            Thread.sleep(1000);     //为了方便测试
            synchronized (nextLock) {
                from.debit(amount);
                to.credit(amount);
            }
        }

        //死锁
        //synchronized (from) {
        //    Thread.sleep(1000);     //为了方便测试
        //    synchronized (to) {
        //        from.debit(amount);
        //        to.credit(amount);
        //    }
        //}
    }

    static class Account {
        long userId;
        int balance;

        public Account(long userId, int balance) {
            this.userId = userId;
            this.balance = balance;
        }

        public long getUserId() {
            return userId;
        }

        public int getBalance() {
            return balance;
        }

        public void debit(int amount) throws Exception {
            if (balance < amount) {
                throw new Exception("余额不足");
            }
            balance -= amount;
        }

        public void credit(int amount) {
            balance += amount;
        }
    }
}
