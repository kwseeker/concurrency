package top.kwseeker.concurrency;

public class Main {

    static int x,y;                        //case1 : 可能出现四种结果 (0,0) (0,1) (1,0) (1,1)
    //static volatile int x; int y;        //case2 : 可能出现四种结果 (0,0) (0,1) (1,0) (1,1)
    //static volatile int x,y;             //case3 : 可能出现三种结果 (0,1) (1,0) (1,1)

    public static void main(String[] args) {

        Thread thread1 = new Thread(() -> {
            Main.x=1;
            System.out.printf("y=%d ", Main.y);
        });
        Thread thread2 = new Thread(() -> {
            Main.y=1;
            System.out.printf("x=%d ", Main.x);
        });

        thread1.start();
        thread2.start();
    }
}
