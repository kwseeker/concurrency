package top.kwseeker.concurrency.concurrent_module.createThread;

/**
 * interrupt()
 * isInterrupted()
 * interrupted()
 */
public class InterruptTest {
    public static void main(String[] args) {

        Thread.currentThread().interrupt();         //设置main线程中断标志位
        printInterrupted(1);

        Object o = new Object();
        try {
            synchronized (o) {
                printInterrupted(2);
                System.out.printf("A Time %d\n", System.currentTimeMillis());   // 中断处理会有一段时间的延迟，所以这句打印出来了而下面那句没有打印出来
                o.wait(100);
                System.out.printf("B Time %d\n", System.currentTimeMillis());
            }
        } catch (InterruptedException ie) {         //抛出异常线程中断标志位被清除
            System.out.printf("WAS interrupted\n");
        }
        System.out.printf("C Time %d\n", System.currentTimeMillis());

        printInterrupted(3);

        Thread.currentThread().interrupt();

        printInterrupted(4);

        try {
            System.out.printf("D Time %d\n", System.currentTimeMillis());
            Thread.sleep(100);
            System.out.printf("E Time %d\n", System.currentTimeMillis());
        } catch (InterruptedException ie) {
            System.out.printf("WAS interrupted\n");
        }
        System.out.printf("F Time %d\n", System.currentTimeMillis());

        printInterrupted(5);

        try {
            System.out.printf("G Time %d\n", System.currentTimeMillis());
            Thread.sleep(100);
            System.out.printf("H Time %d\n", System.currentTimeMillis());
        } catch (InterruptedException ie) {
            System.out.printf("WAS interrupted\n");
        }
        System.out.printf("I Time %d\n", System.currentTimeMillis());

        Thread.currentThread().interrupt();
        System.out.println(Thread.interrupted()?"true":"false");    //带清除中断标志位的isInterrupted(true), 应该是先判断后清除，具体是不是看JVM内部实现
        System.out.println(Thread.interrupted()?"true":"false");
    }

    private static void printInterrupted(int n) {
        System.out.printf("(%d) Am I interrupted? %s\n", n,
                Thread.currentThread().isInterrupted() ? "Yes" : "No");
    }
}