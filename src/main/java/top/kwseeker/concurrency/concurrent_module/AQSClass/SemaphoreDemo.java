package top.kwseeker.concurrency.concurrent_module.AQSClass;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * 信号量
 *      Semaphore 信号量 (java.util.concurrent.Semaphore) 是一个计数信号量
 *      计数信号量由一个指定数量N的"许可"初始化。
 *      每调用一次acquire(), 一个许可会被调用线程取走；
 *      每调用一次release(), 一个许可会被返还给信号量。
 *      在没有许可使用时，无法执行新的线程，从而保护一段代码最多只有N个线程进入。
 *
 *  使用场景
 *      需要控制并发线程数量的场景
 *      （如：连接数据库时，可能设置的JDBC最大连接数为20， 而web服务有200个连接，不加控制的话，会引起连接异常，
 *      这是就可以使用 Semaphore 控制连接数）
 */
@Slf4j
public class SemaphoreDemo {

    //使用Semaphore线程准入，模拟一个抢购的场景：网站做活动，12:00开始0元抢购10件商品
    class Person {
        private int PersonId;

        public Person(int personId) {
            PersonId = personId;
        }

        public int getPersonId() {
            return PersonId;
        }

        public void setPersonId(int personId) {
            PersonId = personId;
        }
    }

    public static void main(String[] args) {
        int productCount = 10;
        ExecutorService executorService = Executors.newCachedThreadPool();
        Semaphore semaphore = new Semaphore(productCount, true);        //公平获取

        //非线程安全
        ArrayList<Integer> arrayList = new ArrayList<>(10);

        //假设有个程序员趁职务之便, 通过代码先抢了3个
        try {
            semaphore.acquire(3);
            arrayList.add(101);
            arrayList.add(102);
            arrayList.add(103);
        } catch (InterruptedException e) {
            log.error("{}", e);
        }

        //假设有1000人一瞬间先后发起请求
        for (int i = 0; i < 1000; i++) {        //一次for循环就是一个封闭空间？
            Person person = new SemaphoreDemo().new Person(i);
            executorService.execute(() -> {
                if (semaphore.tryAcquire()) {
                    synchronized (arrayList.getClass()){
                        arrayList.add(person.getPersonId());
                    }
                }
            });
        }

        executorService.shutdown();
        semaphore.release(productCount);
        //发布中奖名单
        StringBuilder sb = new StringBuilder("中奖人员编号：\n");
        for (int i = 0; i < arrayList.size(); i++) {
            sb.append(arrayList.get(i) + "\n");
        }
        log.info(sb.toString());
    }
}
