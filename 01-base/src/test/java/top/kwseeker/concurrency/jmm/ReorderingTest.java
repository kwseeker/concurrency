package top.kwseeker.concurrency.jmm;

/**
 * (0, 0) 出现的可能原因（内存可见性，指令重排序）
 * 指令重排序：
 * t1:     x=b     a=1
 * t2: y=a　　　b=1
 * 内存可见性：
 * t1: a=1 x=b
 * t2: b=1 y=a
 */
public class ReorderingTest {

    private  static int x = 0, y = 0;
    private  static int a = 0, b = 0;

    public static void main(String[] args) throws InterruptedException {
        int i = 0;
        for (;;){
            i++;
            x = 0; y = 0;
            a = 0; b = 0;
            Thread t1 = new Thread(new Runnable() {
                public void run() {
                    //shortWait(10000);
                    a = 1;
                    x = b;
                    //UnsafeInstance.reflectGetUnsafe().fullFence();
                }
            });

            Thread t2 = new Thread(new Runnable() {
                public void run() {
                    b = 1;
                    //UnsafeInstance.reflectGetUnsafe().fullFence();
                    y = a;
                }
            });

            t1.start();
            t2.start();
            t1.join();
            t2.join();

            String result = "第" + i + "次 (" + x + "," + y + "）";
            System.out.println(result);
            if(x == 0 && y == 0) {
                break;
            }
        }
    }
}
