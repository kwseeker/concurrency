package top.kwseeker.concurrency.jucatomic;

import lombok.Data;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * 使用AtomicXxxFieldUpdater拓展线程安全的方法
 */
@Data
public class FieldThreadSafeUser {
    private String name;
    //private float money;
    //1) 改造 volatile AtomicLongFieldUpdater
    private volatile long money;
    private AtomicLongFieldUpdater updater = AtomicLongFieldUpdater.newUpdater(FieldThreadSafeUser.class, "money");

    //假如有2个业务方法(非线程安全)
    //工资收入
    public float salaryEarn(float count) {
        money += count;
        return money;
    }

    //消费
    public float cost(float count) {
        money -= count;
        return money;
    }

    //2) field线程安全方法改造
    @SuppressWarnings("unchecked")
    public float safeSalaryEarn(long count) {
        return updater.addAndGet(this, count);
    }

    @SuppressWarnings("unchecked")
    public float safeCost(long count) {
        return updater.addAndGet(this, -count);
    }
}
