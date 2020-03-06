package top.kwseeker.concurrency.jucatomic;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class FieldThreadSafeUserTest {

    private FieldThreadSafeUser fieldThreadSafeUser = new FieldThreadSafeUser();

    @Test
    public void testSafeMethod() {
        List<Thread> threads = new ArrayList<>();
        Thread thread1 = new Thread(()->{
            for (int i = 0; i < 10000; i++) {
                //fieldThreadSafeUser.salaryEarn(100);
                fieldThreadSafeUser.safeSalaryEarn(100);
            }
        });
        threads.add(thread1);
        for (int i = 0; i < 10; i++) {
            Thread thread2 = new Thread(()->{
                for (int j = 0; j < 10000; j++) {
                    //fieldThreadSafeUser.cost(1);
                    fieldThreadSafeUser.safeCost(1);
                }
            });
            threads.add(thread2);
        }
        for (Thread thread : threads) {
            thread.start();
        }

        while (Thread.activeCount() > 2) {}
        System.out.println(fieldThreadSafeUser.getMoney());
    }
}