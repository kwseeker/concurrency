package top.kwseeker.concurrency.future;

import org.junit.Test;

import java.util.concurrent.*;
import java.util.function.Supplier;

public class CompletableFutureTest {

    //比如：有个需要异步处理的操作，操作结果还要进一步处理。
    @Test
    public void testFuture() throws Exception {
        Future<String> future = Executors.newFixedThreadPool(1).submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                try {
                    // 异步执行的代码块
                    System.out.println("异步代码块，start...");
                    Thread.sleep(2000);
                    System.out.println("异步代码块，ended");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return "hello";
            }
        });
        String ret = future.get();
        ret += " world";
        System.out.println(ret);

        System.out.println("会阻塞到直到结果返回或超时");
        //如果不想让其阻塞主线程，需要把上面代码整个再丢到一个线程中处理。但是还有简单的方法就是用CompletableFuture。
    }

    // 1 链式处理
    //　 runAsync() 是不带返回值的，supplyAsync是带返回值的
    @Test
    public void testCompletableFuture() throws Exception {
        // supplyAsync() Returns a new CompletableFuture that is asynchronously completed by a task
        // running in the ForkJoinPool.commonPool() with the value obtained by calling the given Supplier.
        CompletableFuture.supplyAsync(() -> {
            try {
                // 异步执行的代码块
                System.out.println("异步代码块，start...");
                Thread.sleep(2000);
                System.out.println("异步代码块，ended");
                return "hello";
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "";
        })
                // thenApply()　方法表示将上步处理结果应用到当前方法
                //.thenApply(ret -> {             //thenApply中的方法和上面代码是在同一个线程中执行的。估计在队列是一先一后的关系。TODO：确认是不是一先以后加入到待处理队列？
                .thenApplyAsync(ret -> {        //thenApplyAsync中的方法在另一个线程中执行。TODO: 看看人家源码怎么实现的，这样封装后代码优雅多了
                    try {
                        System.out.println("异步代码块执行完毕，执行这个方法处理异步代码块的结果...");
                        ret += " world";
                        Thread.sleep(1000);
                        System.out.println("结果进一步处理同样是在异步执行");
                        return ret;
                        //});
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return "";
                }, Executors.newSingleThreadExecutor())    //thenApplyAsync还可以指定使用的线程池
                // thenRun()　方法表示不关心上步处理结果，上一步执行完毕就执行这个
                //.thenRun(() -> {
                .thenRunAsync(() -> {
                    System.out.println("终于轮到我执行了...");
                });
        System.out.println("看看还会阻塞么？");
        Thread.sleep(5000);
        System.out.println("完成");
    }

    //2 组合处理, 两个或多个操作没有先后顺序一起进行
    //  thenCombineXxx()    接收异步操作所有返回值，处理并返回合并结果
    //  thenAcceptBoth()    接收异步操作所有返回值，处理但不返回合并结果
    //  runAfterBothAsync() 不接收异步操作返回值（所有异步操作执行完后执行），无返回值
    @Test
    public void testCompletableFuture2() throws Exception {
        CompletableFuture.supplyAsync(() -> {
            try {
                // 异步执行的代码块1
                System.out.println("异步代码块１，start...");
                Thread.sleep(2000);
                System.out.println("异步代码块１，ended");
                return "hello";
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "";
        }).thenCombine(CompletableFuture.supplyAsync(() -> {
                    //.thenCombineAsync(CompletableFuture.runAsync(() -> {
                    try {
                        System.out.println("异步代码块２，start...");
                        Thread.sleep(1000);
                        System.out.println("异步代码块２，ended");
                        return "world";
                        //});
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return "";
                }, Executors.newSingleThreadExecutor()),
                (ret1, ret2) -> {
                    System.out.println(ret1 + " " + ret2);
                    return ret1 + " " + ret2;
                });          //消耗两个异步处理的结果
        System.out.println("看看还会阻塞么？");
        Thread.sleep(5000);
        System.out.println("完成");
    }

    // thenAcceptBoth() 异步操作有返回值么，合并操作接收异步操作的返回值
    // runAfterBothAsync() 操作有返回值，合并操作不接收返回值
    @Test
    public void testCompletableFuture3() throws Exception {
        CompletableFuture.supplyAsync(() -> {
            try {
                // 异步执行的代码块1
                System.out.println("异步代码块１，start...");
                Thread.sleep(2000);
                System.out.println("异步代码块１，ended");
                return "hello";
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "";
        })
                .thenAcceptBoth(CompletableFuture.supplyAsync(() -> {
                            try {
                                System.out.println("异步代码块２，start...");
                                Thread.sleep(1000);
                                System.out.println("异步代码块２，ended");
                                return "world";
                                //});
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return "";
                        }, Executors.newSingleThreadExecutor()),
                        (ret1, ret2) -> System.out.println(ret1 + " " + ret2))          //消耗两个异步处理的结果
                .runAfterBothAsync(CompletableFuture.supplyAsync(() -> {
                    System.out.println("前面的异步代码执行完后执行...");
                    return "final";
                }), () -> System.out.println("终于都执行完了"));
        System.out.println("看看还会阻塞么？");
        Thread.sleep(5000);
        System.out.println("完成");
    }

    //3 任一处理 任意一个执行完，则结束将结果送到结果处理方法
    //  applyToEither   接收任一异步代码结果，处理并返回
    //  acceptEither    接收任一异步代码结果，处理不返回
    //  runAfterEither  不接收异步代码结果（只要有任一执行完就会执行），也没有返回值
    @Test
    public void testCompletableFuture4() throws Exception {
        CompletableFuture.supplyAsync(() -> {
            try {
                // 异步执行的代码块1
                System.out.println("异步代码块１，start...");
                Thread.sleep(2000);
                System.out.println("异步代码块１，ended");
                return "hello";
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "";
        }).applyToEither(CompletableFuture.supplyAsync(() -> {
                    try {
                        System.out.println("异步代码块２，start...");
                        Thread.sleep(1000);
                        System.out.println("异步代码块２，ended");
                        return "world";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return "";
                }, Executors.newSingleThreadExecutor()),
                ret -> {                                            //结果处理有返回值
                    System.out.println("第一次执行较快结果：" + ret);
                    return ret;
                }).acceptEither(CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("异步代码块３，start...");
                Thread.sleep(1100);
                System.out.println("异步代码块３，ended");
                return "java";
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }), System.out::println);                           //结果处理没有返回值
        System.out.println("看看还会阻塞么？");
        Thread.sleep(5000);
        System.out.println("完成");
    }

    //4 异常补偿
    //  exceptionally 当运行时出现了异常，执行exceptionally进行补偿。
    @Test
    public void testCompletableFuture5() throws Exception {
        String result = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (Math.random() > 0.5) {
                throw new RuntimeException("测试一下异常情况");
            }
            return "hello";
        }).whenComplete((s, t) -> {
            System.out.println("whenComplete()");
            System.out.println("正常返回结果：" + s);
            System.out.println("异常信息" + t.getMessage());
        }).handle((s, t) -> {
            System.out.println("handle()");
            if(t != null) {
                return "返回异常后补偿默认结果：java";
            }
            return "返回正常结果：" + s;
        }).exceptionally(e -> {
            System.out.println("exceptionally()");
            System.out.println(e.getMessage());
            return "hello world";
        }).join();
        System.out.println("结束：" + result);
    }

    /**
     * 源码实现分析
     * CompletableFuture<T> implements Future<T>, CompletionStage<T>
     */
    @Test
    public void testCompletableFutureTheory() throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Void> cf1 = CompletableFuture.runAsync(new VoidTask());
        System.out.println("waiting...");
        //cf1.join();     //同步等待不抛异常
        cf1.get();      //同步等待会抛出异常
        //cf1.get(1, TimeUnit.SECONDS);     //同步等待，最多等待１s
        //cf1.getNow(null);                 //查看任务是否完成并返回，没有则使用默认的结果
        cf1.isCancelled();
        cf1.isCompletedExceptionally();
        cf1.isDone();
        System.out.println("done...");

        //CompletableFuture<Void> cf2 = CompletableFuture.supplyAsync(new VoidTask());

        //cf1.get(1, TimeUnit.SECONDS);

        //CompletableFuture.supplyAsync(() -> {       //提交任务
        //    try {
        //        // 异步执行的代码块
        //        System.out.println("异步代码块，start...");
        //        Thread.sleep(2000);
        //        System.out.println("异步代码块，ended");
        //        return "hello";
        //    } catch (InterruptedException e) {
        //        e.printStackTrace();
        //    }
        //    return "";
        //}).thenApply(ret -> {                       //获取上步任务结果继续处理
        //    try {
        //        System.out.println("异步代码块执行完毕，执行这个方法处理异步代码块的结果...");
        //        ret += " world";
        //        Thread.sleep(1000);
        //        System.out.println("结果进一步处理同样是在异步执行");
        //        return ret;
        //    } catch (Exception e) {
        //        e.printStackTrace();
        //    }
        //    return "";
        //}).thenRun(() -> {                          //前面任务执行完毕后执行，不关心处理结果
        //    System.out.println("终于轮到我执行了...");
        //});
        //Thread.sleep(5000);
        //System.out.println("完成");
    }


    static class VoidTask implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class CallableTask implements Callable {
        @Override
        public Object call() throws Exception {
            Thread.sleep(2000);
            return "some ret";
        }
    }

    //static class SupplierTask implements Supplier
}