package top.kwseeker.concurrency.concurrent_module.thread_confinement;

/**
 * 来自浏览器的每个请求都是一个线程，这种情况下就很适合使用 ThreadLocal 存储请求参数
 * 很方便存取数据，而避免了从请求上层到下层处理的一步步地传参。
 */
public class RequestHolder {

    private final static ThreadLocal<Long> requestHolder = new ThreadLocal<>();

    public static void add(Long id) {
        requestHolder.set(id);
    }

    public static Long getId() {
        return requestHolder.get();
    }

    public static void remove() {
        requestHolder.remove();
    }
}
