package top.kwseeker.concurrency.juccollection.queue;

import org.junit.Test;

public class FinallyTest {

    public int testFinally() {
        int i;
        try {
            System.out.println("try");
            i = 0;
        } finally {
            System.out.println("finally");
            i = -1;
        }
        i = 1;
        return i;
    }

    @Test
    public void test() {
        System.out.println(testFinally());
    }
}
