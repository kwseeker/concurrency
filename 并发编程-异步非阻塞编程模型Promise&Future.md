# Promise & Future 获取异步执行结果

框架源码中这两概念几乎是必现的。

> future和promise起源于[函数式编程](https://zh.wikipedia.org/wiki/函數程式語言)和相关范例（如[逻辑编程](https://zh.wikipedia.org/wiki/邏輯編程) ），目的是将值（future）与其计算方式（promise）分离，从而允许更灵活地进行计算，特别是通过并行化。
>
> Future 表示目标计算的返回值，Promise 表示计算的方式，这个模型将返回结果和计算逻辑分离，目的是为了让计算逻辑不影响返回结果，从而抽象出一套异步编程模型。那计算逻辑如何与结果关联呢？它们之间的纽带就是 callback。

+ **Future出现是为了不需要同步等待获取异步执行结果，期间可以做其他事**

  Future使用参考之前写的FutureTask原理的文档。

  ```java
  //尝试取消
  boolean cancel(boolean mayInterruptIfRunning);
  //返回任务是否执行完成前被取消
  boolean isCancelled();
  //返回任务是否执行完毕
  boolean isDone();
  //阻塞地获取结果
  V get() throws InterruptedException, ExecutionException;
  //带超时的阻塞地获取结果
  V get(long timeout, TimeUnit unit)
          throws InterruptedException, ExecutionException, TimeoutException;
  ```

  + **Future 模式-将来式1**

    ```java
    future.get();
    future.get(10, TimeUnit.SECONDS);
    ```

  + **Future 模式-将来式2**

    ```java
    轮询 isDone() 方法, 返回true,再获取结果
    ```

  + **Future 模式 - 回调式**

    将来式仍然无法完全消除等待。但是可以通过回调式实现。

    + **Netty Future 回调拓展**

      拓展了Future接口，可以在Future<V>实例上添加FutureListener监听器，一旦异步任务执行完毕可以执行监听器回调方法处理结果。

      Netty提供了完整的 Promise、Future 和 Listener 机制，可以将Netty做为并发非阻塞模型框架使用。

      ifeve好像提供了一个参考Netty简版的并发非阻塞模型：https://bitbucket.org/qiyi/commons-future（可以学习下）。

    + **Guava Future 回调拓展**

      和Netty Future 回调使用起来差不到，TODO: 看看它们两者源码实现的差异。 

+ **Future的回调模式比将来式感觉更省资源，但是会带来“回调地狱”问题。为了解决回调地狱引入Promise模式**

  “回调地狱”是指，回调多层嵌套带来的代码不雅观，不易读，难维护的现象。

  + **CompletableFuture**

    这其实就是一种Promise模式的写法。和JS的Promise风格类似。

  + **Netty Promise**

    默认实现类：DefaultPromise
    
    ```java
    private static final int MAX_LISTENER_STACK_DEPTH = Math.min(8, SystemPropertyUtil.getInt("io.netty.defaultPromise.maxListenerStackDepth", 8));
    private static final AtomicReferenceFieldUpdater<DefaultPromise, Object> RESULT_UPDATER = AtomicReferenceFieldUpdater.newUpdater(DefaultPromise.class, Object.class, "result");
    private static final Object SUCCESS = new Object();
    private static final Object UNCANCELLABLE = new Object();
    private static final DefaultPromise.CauseHolder CANCELLATION_CAUSE_HOLDER = new DefaultPromise.CauseHolder(DefaultPromise.StacklessCancellationException.newInstance(DefaultPromise.class, "cancel(...)"));
    private static final StackTraceElement[] CANCELLATION_STACK;
    private volatile Object result;
    // 
    private final EventExecutor executor;
    private Object listeners;
    private short waiters;
    private boolean notifyingListeners;
    ```
    
    测试案例中DefaultPromise的EventExecutor来自于NioEventLoopGroup实例（12个其中之一）。
    
    
    
    ```java
    //NioEventLoopGroup本质是MultithreadEventExecutorGroup, 默认EventExector是ThreadPerTaskExecutor
    executor = new ThreadPerTaskExecutor(this.newDefaultThreadFactory());
    //NioEventLoop(默认12个，对应12个线程)是循环处理异步事件的任务(本身是个任务可以循环监听提交的其他任务的状态)，在某个线程上执行。TODO: 在源码上多加点日志调试下
    //一个任务可以处理多个channel，循环任务的线程在首次提交任务时启动。
    children = new EventExecutor[nThreads];
    children[i] = newChild(executor, args);
    ```
    
    提交任务, 任务被放到下一个EventExector线程中。
    
    ```java
    //任务提交之后是提交到了，任务提交后并不确保立即执行，除非有个需要立即执行的任务或者关闭executor
    loopGroup.schedule(() -> { ... }, 0, TimeUnit.SECONDS);
    ```
    
    DefaultPromise添加监听
    
    ```java
    //添加监听器，然后主动判断一下任务执行完毕了没有，如果结束了就通知所有监听器
    promise.addListener(future -> System.out.println("result: " + promise.get()));
    ```
    
    

+ **响应式编程模式**

  Java9 又出现一种响应式编程模式。

  

## 附录

[IO - 同步，异步，阻塞，非阻塞 （亡羊补牢篇）](https://blog.csdn.net/historyasamirror/article/details/5778378)

[JAVA 拾遗 --Future 模式与 Promise 模式](https://www.cnkirito.moe/future-and-promise/)

[并发编程 Promise, Future 和 Callback](http://ifeve.com/promise-future-callback/)

[Netty 中的异步编程 Future 和 Promise](https://www.cnblogs.com/rickiyang/p/12742091.html)

