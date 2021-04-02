package top.kwseeker.concurrency.jucforkjoin;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

public class RecursiveTaskExecTest {

    /**
     * RecursiveTask是带有返回值的任务
     * 计算累加：１+2+...+10000
     */
    @Test
    public void testExecRecursiveTask() throws ExecutionException, InterruptedException {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        Future<Integer> result = forkJoinPool.submit(new SumTask(1, 100));
        System.out.println(result.get());
        Assert.assertEquals(new Integer(5050), result.get());
    }

    static class SumTask extends RecursiveTask<Integer> {
        private int start;
        private int end;

        public SumTask(int start, int end) {
            this.start = start;
            this.end = end;
        }

        // 约定规则: 假设我们的计算器只能计算最多３个数字的加法，那么当start到end的数字个数<=３时才可以计算，否则拆分
        // 没有达到计算条件就拆分，达到计算条件就计算返回结果
        @Override
        protected Integer compute() {
            // 计算
            if(end - start < 3) {
                System.out.println(String.format("count %d+...+%d in thread:%s", start, end, Thread.currentThread().getName()));
                return add(start, end);
            }
            // 否则拆分
            int mid = (start + end) / 2;
            SumTask subTask1 = new SumTask(start, mid);
            SumTask subTask2 = new SumTask(mid+1, end);
            invokeAll(subTask1, subTask2);                  //拆非的任务通过invokeAll()重新提交
            //subTask1.fork();                              //或者通过fork()提交
            //subTask2.fork();
            return subTask1.join() + subTask2.join();       //通过join()等待子任务结果返回
        }

        private int add(int from, int to) {
            int total = 0;
            for (int i = from; i <= to; i++) {
                total += i;
            }
            return total;
        }
    }
}
