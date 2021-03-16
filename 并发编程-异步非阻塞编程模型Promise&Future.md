# Promise & Future 获取异步执行结果

框架源码中这两概念几乎是必现的。

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
    future.get(10, TimeUnit.Second);
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

    

+ **响应式编程模式**

  Java9 又出现一种响应式编程模式。

  

## 附录

[IO - 同步，异步，阻塞，非阻塞 （亡羊补牢篇）](https://blog.csdn.net/historyasamirror/article/details/5778378)

[JAVA 拾遗 --Future 模式与 Promise 模式](https://www.cnkirito.moe/future-and-promise/)

[并发编程 Promise, Future 和 Callback](http://ifeve.com/promise-future-callback/)

[Netty 中的异步编程 Future 和 Promise](https://www.cnblogs.com/rickiyang/p/12742091.html)