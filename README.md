# Java 并发多线程从简到全

参考：http://ifeve.com/java-concurrency-thread-directory/

目录：

[TOC]

相关文档：

kwseeker/netty Executors线程池.md

## 1 基本概念

#### 1.1 CPU与线程的关系

#### 1.2 线程与进程的区别和关系

#### 1.3 吞吐量  

#### 1.4 线程安全

#### 1.5 线程声明周期  

#### 1.6 守护线程  

#### 1.7 Java内存模型

#### 1.8 可重入

#### 1.9 偏向锁、轻量级锁、重量级锁  

#### 1.10 锁的公平性

#### 1.11 线程组

## 2 多线程基本实现

#### 2.1 多线程实现

+ Interface

    - Runnable
    
    - Callable
    
    - Future
    
    - ExecutorService  
    
+ Class

    - Thread 
    
    - FutureTask
    
        FutureTask 获取线程执行结果的原理：以 ThreadPoolExecutor 为例（实现 ExecutorService 接口）, 其 submit() 方法提交任务， 
        返回 一个 FutureTask 实例，这个实例 outcome 成员变量用于存储线程的执行结果， state 成员变量用于记录线程的执行状态。
        由于不知道什么时候线程执行完毕并返回结果，主线程只能主动轮询查看线程执行状态（FutureTask.get()查看前面说的 state）每次轮询判断线程状态，
        还未执行完则主线程放弃当前时间片竞争下一个时间片，当线程执行完毕后，取 outcome 并返回。
        不过这个是正常的执行流程（对应 FutureTask 里面的 Normal 状态）。
        
        还可能碰到异常状态：抛出异常，线程取消执行或被中断，可以看源码里面是怎么处理异常状态的。
        ```
        private static final int NEW          = 0;
        private static final int COMPLETING   = 1;
        private static final int NORMAL       = 2;
        private static final int EXCEPTIONAL  = 3;
        private static final int CANCELLED    = 4;
        private static final int INTERRUPTING = 5;
        private static final int INTERRUPTED  = 6;
        ``` 
        
        TODO：线程池的线程是怎么将执行结果写入到outcome中的？大概浏览了下源码却没找到！日后仔细研究。

#### 2.2 线程中断与继续执行

+ Thread interrupt() 方法的原理

    虚拟机为每个线程对象维护有一个标志位表示是否有中断请求（当然JDK的源码是看不到这个标识位的，是虚拟机线程实现层面的），
    代表着是否有中断请求。interrupt() 方法调用了 虚拟机interrupt0()方法，将中断标识位置位。
    然后JVM会隔一段时间检测一下中断标志位，JVM因为要平衡性能和反应灵敏度，所以并不是立即处理中断（但是也是非常快的）；
    所以会在一段时间之后抛出中断异常。

## 3 线程安全与同步

#### 3.1 内存机制 volatile

#### 3.2 线程安全类  

##### 3.2.1 线程安全

#### 3.3 线程同步方法

##### 3.3.0 Monitor原理

TODO：JVM Monitor的实现

##### 3.3.1 synchronized

##### 3.3.2 lock

+ ReentrantLock

+ 自旋锁

##### 3.3.3 死锁的产生与解决

##### 3.3.4 AQS CAS

##### 3.3.5 ThreadLocal

+ 实现原理

    参考：[ThreadLocal和ThreadLocalMap源码分析](https://www.cnblogs.com/KingJack/p/10599921.html)

    总结：ThreadLocal实现线程本地变量依赖于Thread对象ThreadLocalMap类型的成员变量threadLocals，
    ThreadLocalMap是一个哈希表（通过Hash计算索引的数组），ThreadLocal变量用于作为key索引这个ThreadLocal在某个线程中对应的值，
    如果万一发生碰撞(求出的索引处已经有值，key不相同)，则尝试向下一个索引处插入，每次set()后都要清理一下无效的节点并判断是否需要扩容。

    ![ThreadLocal数据结构](https://upload-images.jianshu.io/upload_images/7432604-ad2ff581127ba8cc.jpg?imageMogr2/auto-orient/)
    比如有个类实例，类实例有两个ThreadLocal成员变量分别是ThreadLocal1和ThreadLocal2，有三个线程引用这个类实例，则它们的内存结构如上。

    ![执行流程](https://img-blog.csdnimg.cn/20190326141858895.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20xbl9sb3Zl,size_16,color_FFFFFF,t_70)
    
    - set实现
    
        在某个线程中对ThreadLocal变量做set(value)操作，会首先获取当前线程对象，然后判断当前线程的 threadLocals (ThreadLocalMap，
        每个线程都有这个成员变量) 是否为空;
        不为空，则直接插入这个值value；
        ```
        private void set(ThreadLocal<?> key, Object value) {
        
            // We don't use a fast path as with get() because it is at
            // least as common to use set() to create new entries as
            // it is to replace existing ones, in which case, a fast
            // path would fail more often than not.
        
            Entry[] tab = table;
            int len = tab.length;
            int i = key.threadLocalHashCode & (len-1);
        
            for (Entry e = tab[i];
                 e != null;
                 e = tab[i = nextIndex(i, len)]) {
                ThreadLocal<?> k = e.get();
        
                if (k == key) {
                    e.value = value;
                    return;
                }
        
                if (k == null) {
                    replaceStaleEntry(key, value, i);
                    return;
                }
            }
        
            tab[i] = new Entry(key, value);
            int sz = ++size;
            if (!cleanSomeSlots(i, sz) && sz >= threshold)
                rehash();
        }
        ```
        
        为空，则新建一个 ThreadLocalMap，并设置第一个值为value；
        ```
        ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue) {
            table = new Entry[INITIAL_CAPACITY];
            int i = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1);
            table[i] = new Entry(firstKey, firstValue);
            size = 1;
            setThreshold(INITIAL_CAPACITY);
        }
        ```
        
        ThreadLocalMap 内部是一个哈希表（即通过Hash索引的数组，默认容量16，使用当前 ThreadLocal对象的 threadLocalHashCode & (INITIAL_CAPACITY -1) 作为索引 ），
        每一个成员 Entry 都是一个包含了值的 ThreadLocal 的弱引用。
        
        ThreadLocalMap.set()位置索引：键的hash值与（len-1）按位与获得数组index，
        然后判断这个索引处（Entry extends WeakReference<ThreadLocal<?>>）是否为空，为空直接插入；
        非空则判断key是否相等，不相等直接插入，相等则替换值。
        
    - get实现
    
    - remove实现
    
    InheritableThreadLocal类是ThreadLocal的子类。InheritableThreadLocal允许一个线程创建的所有子线程访问其父线程的值。
    
    Ps：
    JDK8之前静态变量存储在方法区(方法区是JVM的规范，永久代是方法区的具体实现),JDK8之后就取消了“永久代”，取而代之的是“元空间”，
    永久代中的数据也进行了迁移，静态成员变量迁移到了堆中。

+ 内存泄漏隐患与内存泄漏检测

    [使用ThreadLocal不当可能会导致内存泄露](http://ifeve.com/%E4%BD%BF%E7%94%A8threadlocal%E4%B8%8D%E5%BD%93%E5%8F%AF%E8%83%BD%E4%BC%9A%E5%AF%BC%E8%87%B4%E5%86%85%E5%AD%98%E6%B3%84%E9%9C%B2/)

    为何ThreadLocal容易造成内存泄漏？  
    
    ```
    static class Entry extends WeakReference<ThreadLocal<?>> {...}
    ```
    首先ThreadLocal.set() 设置的值存储在线程的Entry数组threadLocals中，数组成员是Entry类型是ThreadLocal变量的WeakReference。
    因此如果ThreadLocal没有外部强引用来引用它，那么Entry会在下次JVM垃圾收集时被回收。
    出现一个null Key的情况，外部读取ThreadLocalMap中的元素是无法通过null Key来找到Value的。
    因此如果当前线程的生命周期很长，一直存在，那么其内部的ThreadLocalMap对象也一直生存下来，
    这些null key就存在一条强引用链的关系一直存在：Thread --> ThreadLocalMap-->Entry-->Value，
    这条强引用链会导致Entry不会回收，Value也不会回收，但Entry中的Key却已经被回收的情况，造成内存泄漏。
   
    JVM团队做了一些措施来保证ThreadLocal尽量不会内存泄漏：在ThreadLocal的get()、set()、remove()方法调用的时候
    会清除掉线程ThreadLocalMap中所有Entry中Key为null的Value，并将整个Entry设置为null，利于下次内存回收。
    
    可以使用 Executors.newFixedThreadPool() 创建一个持久的线程，在线程中不断地新建ThreadLocal变量,set(),然后变量赋值null；
    慢慢地会发现这个测试进程占用内存会越来越大。

    Ps：
    软引用、弱引用、虚引用可以理解为强引用的镜像，强引用失效后，软引用会在内存不足时被自动回收，弱引用会在下一次GC时被回收，
    虚引用可以在任何时候被回收了。

## 4 JUC

#### 4.1 Future 及其实现类

+ 超时监听
    
    - 多线程超时监听与退出 
    
        TODO: [java 监听多线程超时：Future](https://blog.csdn.net/yiyiwyf/article/details/80065413)

+ 获取线程执行结果


#### Java异步与回调

+ Java异步实现
    
    这里说的异步指任务异步执行+异步处理执行结果；
    多线程本身是异步的，但是Java8之前异步有个问题：对于有返回值的任务只能同步等待它返回结果。
    Java8之前会不断轮询任务线程是否执行完。
    
    事件驱动（事件循环，请求对象，执行回调）。  
    真正的异步都是借助回调实现的。异步任务提交的同时传一个回调函数，当任务完成异步任务线程执行回调。
    
    - Future（伪异步：需要同步获取结果）   
   
        Future 的限制：  
        无法手动完成；  
        阻塞式结果返回；
        无法链式多个Future；
        无法合并多个Future结果；
        缺少异常处理。
        
    - CountdownLatch（伪异步）
    
    - CompletableFuture（Java8新增接口，实现真正的异步）
        
        针对Future的限制，Java8引入了CompletableFuture。
        ```
        CompletableFuture asyncCompletableFuture3 = CompletableFuture.supplyAsync(() -> {
                    return String.format("[Thread: %s] Hello world ...", Thread.currentThread().getName());
                }).thenApply(value -> {
                    return value + " at " + LocalDate.now();
                }).thenApply(value -> {
                    System.out.println(value);
                    return value;                               //异步返回结果 + 合并Future结果
                }).thenRun(() -> {
                    System.out.println("操作结束");
                }).exceptionally((e)-> {                        //异常处理
                    e.printStackTrace();
                    return null;
                });
        ```
        
    - 第三方框架的实现
    
        * Guava
        
        * Netty Promise

    - Spring中异步方法
        
        @Async

+ Java回调实现

+ 对Java异步请求执行同步监听

    - Future get()方法阻塞等待异步任务执行结果
        
        ```
        /**
         * Awaits completion or aborts on interrupt or timeout.
         *
         * @param timed true if use timed waits
         * @param nanos time to wait, if timed
         * @return state upon completion
         */
        private int awaitDone(boolean timed, long nanos)
            throws InterruptedException {
            final long deadline = timed ? System.nanoTime() + nanos : 0L;
            WaitNode q = null;
            boolean queued = false;
            for (;;) {
                if (Thread.interrupted()) {
                    removeWaiter(q);
                    throw new InterruptedException();
                }
    
                int s = state;
                if (s > COMPLETING) {
                    if (q != null)
                        q.thread = null;
                    return s;
                }
                else if (s == COMPLETING) // cannot time out yet
                    Thread.yield();         //放弃当前执行机会，从执行状态变为可执行状态，等待下次执行机会
                else if (q == null)
                    q = new WaitNode();
                else if (!queued)
                    queued = UNSAFE.compareAndSwapObject(this, waitersOffset,
                                                         q.next = waiters, q);
                else if (timed) {
                    nanos = deadline - System.nanoTime();
                    if (nanos <= 0L) {
                        removeWaiter(q);
                        return state;
                    }
                    LockSupport.parkNanos(this, nanos);
                }
                else
                    LockSupport.park(this);
            }
        }
        ```

    - 使用wait和notify方法
    
    - 使用条件锁
    
    - 使用CountDownLatch
    
    - 使用CyclicBarrier

## 5 线程池  

#### 5.1 实现原理

#### 5.2 几种实现


## 6 线程间通信

#### 6.1 JUC线程安全的阻塞队列

#### 6.2 CountDownLatch  

TODO: [什么时候使用CountDownLatch](http://www.importnew.com/15731.html)

#### 6.3 CyclicBarrier

TODO: CountDownLatch 和 CyclicBarrier 的区别

#### 6.4 Semaphore

#### 6.5 FutureTask

## 7 Fork/Join


## 8 线程监控与调试

#### 8.1 


## 9 实际应用案例
