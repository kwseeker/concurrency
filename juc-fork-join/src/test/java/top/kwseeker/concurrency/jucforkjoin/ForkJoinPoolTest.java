package top.kwseeker.concurrency.jucforkjoin;

import org.junit.Test;

import java.util.concurrent.*;

/**
 * 框架代码里面ForkJoin还真不少见
 */
public class ForkJoinPoolTest {

    @Test
    public void testForkJoinExec() throws ExecutionException, InterruptedException {
        // 1 创建线程池 ------------------------------------------------------------------------------
        //   parallelism:最大并发度，与处理器核心数量相同，但不能超过0x7fff, 构造方法会校验
        //   asyncMode: TODO ?
        //   ctl: TODO ?
        //ForkJoinPool forkJoinPool = new ForkJoinPool();
        //ForkJoinPool forkJoinPool = new ForkJoinPool(8);
        ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors(),
                new MyForkJoinWorkerThreadFactory(),
                null,
                false);
        // 2 提交任务 -------------------------------------------------------------------------------
        // 2.1 无返回值
        //forkJoinPool.execute();
        // 2.2
        //forkJoinPool.invoke();
        //forkJoinPool.invokeAll();
        // 2.3 入参：Runnable Callable ForkJoinTask 返回 ForkJoinTask
        //     ForkJoinTask 有非常多的子类，分布在jdk源码的各个地方
        //     ForkJoinTask 实现了　Future<V> 接口
        Future<Integer> result = forkJoinPool.submit(new MyForkJoinTask(1, 100));
        // 3 获取执行结果 -----------------------------------------------------------------------------
        System.out.println(result.get());
    }

    static final class MyForkJoinTask extends ForkJoinTask<Integer> {
        Integer result;
        private final int start;
        private final int end;

        public MyForkJoinTask(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        protected boolean exec() {
            // 计算
            if(end - start < 3) {
                System.out.println(String.format("count %d+...+%d in thread:%s", start, end, Thread.currentThread().getName()));
                result = add(start, end);
                return true;
            }
            // 否则拆分
            int mid = (start + end) / 2;
            RecursiveTaskExecTest.SumTask subTask1 = new RecursiveTaskExecTest.SumTask(start, mid);
            RecursiveTaskExecTest.SumTask subTask2 = new RecursiveTaskExecTest.SumTask(mid+1, end);
            subTask1.fork();                              //或者通过fork()提交
            subTask2.fork();
            //invokeAll(subTask1, subTask2);                  //拆非的任务通过invokeAll()重新提交
            result = subTask1.join() + subTask2.join();       //通过join()等待子任务结果返回
            return true;
        }

        private int add(int from, int to) {
            int total = 0;
            for (int i = from; i <= to; i++) {
                total += i;
            }
            return total;
        }

        @Override
        public Integer getRawResult() {
            return result;
        }
        @Override
        protected void setRawResult(Integer result) {
            this.result = result;
        }
    }

    static final class MyForkJoinWorkerThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {
        @Override
        public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            return new MyForkJoinWorkerThread(pool);
        }
    }

    static final class MyForkJoinWorkerThread extends ForkJoinWorkerThread {
        protected MyForkJoinWorkerThread(ForkJoinPool pool) {
            super(pool);
        }
    }
}
