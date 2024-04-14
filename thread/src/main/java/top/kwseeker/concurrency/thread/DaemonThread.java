package top.kwseeker.concurrency.thread;

/**
 * 守护线程特点
 * 守护进程（Daemon）是运行在后台的一种特殊进程。
 * 它独立于控制终端并且用于周期性地执行某种任务或等待处理某些发生的事件。
 * 线程和主线程的生命周期是独立的，即线程不会随着主线程的退出而退出，只要还有一个非守护线程，JVM进程就不会退出；
 * 如果线程全部是守护线程，JVM进程会退出，并会杀死守护线程。
 */
public class DaemonThread {

    public static void main(String[] args) throws InterruptedException {
        Thread daemonThread = new Thread(() -> {
            try {
                while(true) {
                    System.out.println("守护线程，打印...");
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                System.out.println(">>>>>>> 守护线程被中断");  //没有打印被中断信息，直接被暴力杀死了
                                                             //程序退出时需要小心守护线程被暴力杀死导致有些东西没有被及时保存
                //... 中断后执行资源清理、保存等操作
                throw new RuntimeException(e);
            } finally {
                System.out.println(">>>>>>> 守护线程退出");
            }
        });
        daemonThread.setDaemon(true);

        Thread aThread = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    System.out.println("普通线程，打印...");
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                System.out.println(">>>>>>>  普通线程退出");
            }
        });

        daemonThread.start();
        aThread.start();

        int daemonThreadDefaultPriority = daemonThread.getPriority();
        System.out.println("守护线程默认优先级：" + daemonThreadDefaultPriority);
        int normalThreadDefaultPriority = aThread.getPriority();
        System.out.println("普通线程默认优先级：" + normalThreadDefaultPriority);

        Thread.sleep(3500);

        //守护线程默认会在所有非守护线程都退出之后被JVM强制关闭，为了防止这种暴力行为导致难以挽回的结果
        //应该使用关闭钩子中断守护线程，守护线程中监听中断信号或异常执行退出前的处理
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("执行关闭钩子");
            daemonThread.interrupt();
        }));
        System.out.println(">>>>>>> 主线程退出");
    }

    //public static void main(String[] args) throws InterruptedException {
    //    Thread daemonThread = new Thread(() -> {
    //        try {
    //            while (true) {
    //                System.out.println("守护线程，打印...");
    //                Thread.sleep(1000);
    //            }
    //        } catch (InterruptedException e) {
    //            throw new RuntimeException(e);
    //        } finally {
    //            System.out.println(">>>>>>> 守护线程退出");
    //        }
    //    });
    //    daemonThread.setDaemon(true);
    //    daemonThread.start();
    //
    //    Thread.sleep(100);
    //    System.out.println(">>>>>>> 主线程退出");
    //}
}
