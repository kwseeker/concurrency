# ForkJoinPool 

工作中虽没用到，但在框架中却见了好多次。

使用案例：Stream并行模式，...

## 前置知识

+ **分治法**（任务拆分最终合并）

  fork: 任务拆分，join: 结果合并。

+ **工作窃取**

  工作窃取是指当某个线程的任务队列中没有可执行任务的时候，从其他线程的任务队列中窃取任务来执行。

  ForkJoinPool工作线程在和自己绑定的工作队列上存取任务是LIFO,而窃取其他线程的任务的时候，从队列头部取获取(FIFO)。

  > 为何从自己的工作队列上存取是LIFO, 从其他线程的工作队列上取是FIFO?
  >
  > 这样做的结果是被偷走的总是当前队列中最重的任务，这样任务量相对分配地也更加均匀。

+ **如果自己设计会遇到的疑问**

  + 任务拆分后很多子孙任务，怎么分配线程处理？

    创建多少个线程，什么情况下创建新线程（创建时机）？

  + 任务偷取怎么保证并发安全？
  + 父任务处理线程会不会等待子任务处理完成？



## 实现原理

**ForkJoinPool基本组成**

```java
    // Instance fields
    volatile long ctl;                   // main pool control
    volatile int runState;               // lockable status
    final int config;                    // parallelism, mode
    int indexSeed;                       // to generate worker index
    volatile WorkQueue[] workQueues;     // main registry
    final ForkJoinWorkerThreadFactory factory;
    final UncaughtExceptionHandler ueh;  // per-worker UEH
    final String workerNamePrefix;       // to create worker name string
    volatile AtomicLong stealCounter;    // also used as sync monitor
```

主要由一组WorkQueue组成。另外还创建了一个公有的`ForkJoinPool common`。

**外部任务提交：`externalPush(task);`**





## 附录

+ `ForkJoinTask`

  定义ForkJoin任务的基本抽象类。Stream中众多的操作类都是此类的子类。

  源码注释翻译：

  用于定义在`ForkJoinPool`中执行的任务的抽象类。是类似于线程的实体但是轻量地多（？）。
  在一个`ForkJoinPool`中大量的任务和子任务可能由很少量的实际线程承载，以一些使用限制为代价。

  一个“主”`ForkJoinTask`在显示提交给{@link ForkJoinPool}时开始执行，或者，如果还没有参与到ForkJoin计算中，则通过{@link #fork}、{@link #invoke}或相关方法在{@link ForkJoinPool#commonPool()}中开始执行(?)。

  一旦启动，它通常会依次启动其他子任务。

  从这个类的名称可以看出，许多使用{@code ForkJoinTask}的程序只使用{@link #fork}和{@link #join}方法,或者衍生的方法，如{@link #invokeAll(ForkJoinTask…) invokeAll}。

  但是，这个类还提供了许多其他方法，可以在高级用法中发挥作用，以及支持新形式的fork/join处理的扩展机制。

  {@code ForkJoinTask}是{@link Future}的轻量级形式。

  {@code ForkJoinTask}的效率源于一组限制(只是部分静态强制执行)，反映其主要用途为计算任务计算纯函数或操作纯粹孤立的对象。

  主要的协调机制是{@link #fork}，它安排异步执行；{@link #join}，它直到计算完任务的结果才继续执行。

  理想情况下，计算应该避免{@code synchronized}方法或块，并且除了连接其他任务或使用与fork/join调度协作的同步器(如“Phasers”)之外，还应该最小化其他阻塞同步。可细分的任务也不应该执行阻塞I/O，理想情况下应该访问完全独立于其他正在运行的任务所访问的变量。通过不允许抛出{@code IOExceptions}等检查过的异常，可以松散地执行这些准则。然而，计算可能仍然会遇到未检查的异常，这些异常会被重新抛出给试图加入它们的调用者。这些异常还可能包括{@link RejectedExecutionException}，它源于内部资源的耗尽，比如分配内部任务队列的失败。重新抛出的异常与常规异常的行为方式相同，但在可能的情况下，包含堆栈跟踪 (例如使用{@code ex.printStackTrace()}显示启动计算的线程和实际遇到异常的线程;至少是后者。

  定义和使用可能阻塞的forkjointask是可能的，但这么做需要进一步考虑三个事项:

  (1) 如果任何其他任务应该依赖于阻塞外部同步或I / O的任务，则完成很少。事件风格的异步任务从未加入（例如，这些子类CountedCompleter ）通常属于此类别。

  (2) 为了减少资源影响，任务应该小;理想情况下，只执行(可能的)阻塞操作。

  (3)除非使用{@link ForkJoinPool.ManagedBlocker} API，或者已知可能被阻塞的任务数小于池的{@link ForkJoinPool#getParallelism}级别，否则池不能保证有足够的线程可用，以确保进程或性能良好。

  等待完成和提取任务结果的主要方法是{@link #join}，但是有几个变体:

  {@link Future#get}支持可中断 和/或 定时等待完成并使用{@code Future}约定报告结果。

  {@link #invoke}在语义上等价于{@code fork();}但总是试图在当前线程中开始执行。这些方法的“<em>quiet</em>”形式不提取结果或报告异常。当执行一组任务时，这些可能很有用，并且需要将结果或异常的处理延迟到全部完成时。

  {@code invokeAll}(在多个版本中可用)执行最常见的并行调用形式: forking一组任务并将它们全部joining起来。

   在大多数典型的用法中，fork-join对的作用类似于一个调用(fork)和一个并行递归函数的返回(join)与其他形式的递归调用一样，返回（连接）应该是最内层的。例如，{@code a.fork();b.fork ();b.join ();a.join();}可能比在{@code b}之前加入{@code a}要有效得多。

  任务的执行状态可以在几个层次上详细查询:
  如果任务以任何方式完成(包括在没有执行的情况下取消任务)，则{@link #isDone}为真;
  如果任务在没有取消或遇到异常的情况下完成，则{@link # iscompletednormal}为真;
  如果任务被取消，则{@link #isCancelled}为真(在这种情况下，{@link #getException}返回一个{@link java.util.concurrent.CancellationException});
  如果任务被取消或遇到异常，则{@link #isCompletedAbnormally}为真，在这种情况下{@link #getException}将返回遇到的异常或{@link java.util.concurrent.CancellationException}。

  ForkJoinTask类通常不直接子类化。相反，您可以子类化一个抽象类，它支持一种特定的fork/join处理风格，通常{@link RecursiveAction}用于不返回结果的大多数计算，{@link RecursiveTask}用于返回结果的计算，{@link CountedCompleter}用于那些完成的操作触发其他操作的计算。

  通常，一个具体的ForkJoinTask子类声明包含其参数的字段，在构造函数中建立，然后定义一个{@code compute}方法，以某种方式使用这个基类提供的控制方法。

  方法{@link #join}及其变体仅适用于完成依赖项为非循环的情况;也就是说，并行计算可以描述为一个有向无环图(DAG)。否则，在任务循环地相互等待时，执行可能会遇到某种形式的死锁。但是，这个框架支持其他方法和技术(例如使用{@link Phaser}、{@link #helpQuiesce}和{@link #complete})，这些方法和技术可以用于为非静态结构为DAGs的问题构造自定义子类。为了支持这种用法，可以使用{@link #setForkJoinTaskTag}或{@link #compareAndSetForkJoinTaskTag} 原子标记一个{@code short}值，并使用{@link #getForkJoinTaskTag}检查。ForkJoinTask实现没有为任何目的使用这些{@code protected}方法或标记，但它们可能用于特殊子类的构造。例如，并行图遍历可以使用提供的方法来避免重新访问已经处理过的节点/任务。(用于标记的方法名称非常庞大，部分原因是为了鼓励对反映其使用模式的方法进行定义。)

  大多数基本支持方法是{@code final}，以防止覆盖本质上与底层轻量级任务调度框架相关的实现。开发者要创建新的基于fork/join风格的处理，至少要实现{@code protected} 方法： {@link #exec}, {@link　#setRawResult}, 还有 {@link #getRawResult},  同时还引入了一个抽象的计算方法可以在它的子类中实现，可能依赖于这个类提供的其他{@code protected}方法。

  ForkJoinTasks应该执行相对少量的计算。 通常应通过递归分解将大型任务分解为较小的子任务。 作为一个非常粗略的经验法则，任务应执行多于100个且少于10000个基本计算步骤，并应避免无限循环。 如果任务太大，则并行性无法提高吞吐量。 如果太小，则内存和内部任务维护开销可能会使处理不堪重负。

  此类为{@link Runnable}和{@link Callable}提供了{@code Adapt}方法，这些方法在将{@code ForkJoinTasks}的执行与其他类型的任务混合使用时可能会有用。 当所有任务都采用这种形式时，请考虑使用用asyncMode构造的池。

   ForkJoinTasks是{@code Serializable}，这使得它们可以在诸如远程执行框架之类的扩展中使用。 仅在执行之前或之后而不是在执行过程中序列化任务是明智的。 执行本身不依赖序列化。

+ **参考**：

  发现有人深入的研究了这部分代码，讲得也比较详细。可以写些demo结合博客调试一下。

  [A Java Fork/Join Framework（Doug Lea 关于java Fork/Join框架的论文翻译）](https://blog.csdn.net/dhaibo1986/article/details/108727249)

  [java线程池(四)：ForkJoinPool的使用及基本原理](https://blog.csdn.net/dhaibo1986/article/details/108737347)

  [java线程池(五)：ForkJoinPool源码分析之一(外部提交及worker执行过程)](https://blog.csdn.net/dhaibo1986/article/details/108782916)

  [java线程池(六)：ForkJoinPool源码分析之二(WorkQueue源码)](https://blog.csdn.net/dhaibo1986/article/details/108801254)

  [java线程池(七)：ForkJoinPool源码分析之三(ForkJoinTask源码)](https://blog.csdn.net/dhaibo1986/article/details/109208806)

  [java线程池(八)：ForkJoinPool源码分析之四（ForkJoinWorkerThread源码）](https://blog.csdn.net/dhaibo1986/article/details/109221853)

