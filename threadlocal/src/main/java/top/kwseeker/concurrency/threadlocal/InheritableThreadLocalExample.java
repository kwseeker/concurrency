package top.kwseeker.concurrency.threadlocal;

//InheritableThreadLocal 支持在线程切换时传递父线程的上下文到子线程中
//不能反向传递、一旦传递完成不再受到父线程修改影响
public class InheritableThreadLocalExample {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("ThreadLocal count in main: " +  TLHolder.incrementAndGet());

        Thread parent = new Thread(() -> {
            sleep(1000);
            System.out.println("ThreadLocal count in parent: " +  TLHolder.incrementAndGet());

            Thread child = new Thread(() -> {
                sleep(1000);
                System.out.println("ThreadLocal count in child: " +  TLHolder.incrementAndGet());
            });
            child.start();

            try {
                child.join();
            } catch (InterruptedException ignored) {
            }
        });
        parent.start();

        sleep(1500);
        System.out.println("ThreadLocal count in main: " +  TLHolder.incrementAndGet());

        parent.join();
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
    }
    static class TLHolder {
        private static final ThreadLocal<Integer> tc = new InheritableThreadLocal<>();

        public static int get() {
            return tc.get();
        }

        public static void set(int value) {
            tc.set(value);
        }

        public static int incrementAndGet() {
            Integer count = tc.get();
            if (count == null) {
                tc.set(1);
                return 1;
            }
            tc.set(++count);
            return count;
        }

        public static void remove() {
            tc.remove();
        }
    }
}
