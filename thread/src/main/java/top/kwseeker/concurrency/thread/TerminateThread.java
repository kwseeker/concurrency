package top.kwseeker.concurrency.thread;

/**
 * Thread stop() 测试
 * 会在线程任务体内抛出 ThreadDeath 错误，
 */
public class TerminateThread {

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(() -> {
            try {
                long current = System.currentTimeMillis();
                while(true) {
                    if (System.currentTimeMillis() - current >= 200) {
                        System.out.print(".");
                        current = System.currentTimeMillis();
                    }

                    //if (Thread.interrupted()) {
                    //    System.out.print("Ignore");
                    //}

                    //线程RUNNABLE(READY/RUNNING)状态不会被强制中断，需要手动判断状态退出线程，这样很安全。
                    //if (Thread.currentThread().isInterrupted()) {
                    //    System.out.print("continue");
                    //    //break;
                    //}
                }
            } finally {
                System.out.println("Terminated");
            }
        });
        thread.start();
        Thread.sleep(2000);
        //thread.stop();
        thread.interrupt();
    }

    //public static void main(String[] args) throws InterruptedException {
    //    Thread thread = new Thread(() -> {
    //        try {
    //            long current = System.currentTimeMillis();
    //            while(true) {
    //                if (System.currentTimeMillis() - current >= 200) {
    //                    System.out.print(".");
    //                    current = System.currentTimeMillis();
    //                }
    //            }
    //        } catch (Error e) { //ThreadDeath 是个 Error
    //            e.printStackTrace();
    //        } finally {
    //            System.out.println("Terminated");
    //        }
    //    });
    //    thread.start();
    //    Thread.sleep(2000);
    //    thread.stop();
    //}
}
