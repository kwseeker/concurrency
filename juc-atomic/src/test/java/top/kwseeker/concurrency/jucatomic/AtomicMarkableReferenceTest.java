package top.kwseeker.concurrency.jucatomic;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * 个人感觉这个类挺适合“只执行一次”的场景的，比如多个线程都可以执行初始化但是初始化只能执行一次。
 */
public class AtomicMarkableReferenceTest {

    /**
     * 模拟负载均衡某个服务A可以选择底层服务B（分布式）的某个节点作为provider,
     * 服务A只有用到服务B才会临时选择节点（可能存在多个线程并发执行节点选择），一旦选择后就一直使用这个节点。
     */
    @Test
    public void testInitOnce() throws Exception {
        Provider provider = new Provider("");   //未初始化的服务节点引用
        AtomicMarkableReference<Provider> amr = new AtomicMarkableReference<>(provider, false);

        //只可能有一个线程能执行节点选择
        CountDownLatch latch = new CountDownLatch(100);
        for (int i = 1; i <= 100; i++) {
            new Thread(() -> {
                try {
                    latch.await();

                    //Provider reference = amr.getReference();
                    //boolean marked = amr.isMarked();
                    //随机选择一个节点
                    String nodeNo = String.valueOf(ThreadLocalRandom.current().nextInt(1, 100));
                    boolean ret = amr.compareAndSet(provider, new Provider(nodeNo), false, true);
                    if (ret) {
                        System.out.println("node init done: " + nodeNo);
                    }

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
            latch.countDown();
        }

        Thread.sleep(100);
        System.out.println("node: " + amr.getReference().getNode());
    }

    @Data
    @AllArgsConstructor
    static class Provider {
        String node;
    }
}
