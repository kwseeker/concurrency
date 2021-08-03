package top.kwseeker.concurrency.jmm.feature;

/**
 * 可见性测试
 * 1) 通过 volatile 确保变量的可见性
 */
public class VisibilityTest {

    //private static volatile boolean initFlag = false;     //可以确保及时看到最新的值
    private static boolean initFlag = false;

    // 不通过给initFlag加volatile，让initFlag值对threadA可见，可能不是及时可见, 存在某些机制捎带着把initFlag给刷新了
    // Amazing !!!
    // 1 可能因为空间局部性，刷新counter时将initFlag一起给刷新了,两者可能在同一个缓存行
    //private volatile static int counter = 0;
    // 2
    //private static Integer counter = 0;

    public static void refresh(){
        System.out.println("refresh data ...");
        initFlag = true;
        System.out.println("refresh data success");
    }

    public static void main(String[] args) throws InterruptedException {
        Thread threadA = new Thread(()->{
            System.out.println("线程：" + Thread.currentThread().getName() + " running ...");
            while (!initFlag){
                // 1 2
                //counter++;
                // 3    这种情况可能存在线程上下文切换，上下文切换回来的时候可能刷新initFlag值
                System.out.println("running ...");
            }
            System.out.println("线程：" + Thread.currentThread().getName() + "当前线程嗅探到initFlag的状态的改变");
        },"threadA");
        threadA.start();

        Thread.sleep(2000);

        Thread threadB = new Thread(VisibilityTest::refresh,"threadB");
        threadB.start();
    }
}
