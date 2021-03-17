package top.kwseeker.concurrency.promise;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Netty Future Promise 模式
 */
public class PromiseTest {

    @Test
    public void testNettyPromise() throws InterruptedException {
        //本质就是个线程池
        NioEventLoopGroup loopGroup = new NioEventLoopGroup();
        //存储异步任务执行结果
        DefaultPromise<String> promise = new DefaultPromise<>(loopGroup.next());
        //往线程池提交任务
        loopGroup.schedule(() -> {
            Thread.sleep(1000);
            System.out.println("异步任务1执行结束 at " + System.currentTimeMillis());
            promise.setSuccess("task " + 1 + " 执行成功");
            return promise;
        }, 0, TimeUnit.SECONDS);
        loopGroup.schedule(() -> {
            Thread.sleep(800);
            System.out.println("异步任务2执行结束 at " + System.currentTimeMillis());
            promise.setSuccess("task " + 2 + " 执行成功");
            return promise;
        }, 0, TimeUnit.SECONDS);

        {   //为何异步任务已经完成再执行注册还是可以读到返回值　TODO: 是不是注册回调时，如果Promise中有值就会调所有回调方法？只会调用一次？
            Thread.sleep(2000);
            System.out.println(System.currentTimeMillis());
        }
        //注册Promise监听，有结果返回就执行对应的回调
        promise.addListener(future -> System.out.println("result: " + promise.get()));
        promise.addListener(future -> System.out.println("result: " + promise.get()));

        loopGroup.awaitTermination(10, TimeUnit.SECONDS);
    }
}
