# 并发编程-JUC集合

JUC中的集合：

在JU的集合基础上，加了锁、阻塞、优先级等拓展。

**List**:

+ **CopyOnWriteArrayList**

**Queue**：

+ BlockingQueue (I)
  + **ArrayBlockingQueue**
  + **LinkedBlockingQueue**
  + **ConcurrentLinkedQueue**
  + **DelayQueue**
  + **PriorityBlockingQueue**
  + **SynchronousQueue**
+ BlockingDeque (I)
  + **ConcurrentLinkedDeque**
  + LinkedBlockingDeque
+ TransferQueue (I)
  + LinkedTransferQueue

**Map**:

+ ConcurrentMap (I)
  + **ConcurrentHashMap**
  + ConcurrentSkipListMap

**Set**:

+ ConcurrentSkipListSet
+ **CopyOnWriteArraySet**



## 队列

### PriorityQueue (java.util)

原理其实比较简单，数据结构是二叉堆（最小堆）。可以通过修改`Comparator<? super E> comparator`修改排序规则。

> 堆是一种经过排序的[完全二叉树](https://baike.baidu.com/item/完全二叉树/7773232?fromModule=lemma_inlink)，父节点大于或小于所有子节点，左右子节点的大小不确定。
>
> [最大堆](https://baike.baidu.com/item/最大堆/4633143?fromModule=lemma_inlink)和最小堆是[二叉堆](https://baike.baidu.com/item/二叉堆/10978086?fromModule=lemma_inlink)的两种形式。
>
> 最大堆：根结点的键值是所有堆结点键值中最大者。
>
> 最小堆：根结点的键值是所有堆结点键值中最小者。

```java
//默认初始容量
private static final int DEFAULT_INITIAL_CAPACITY = 11;
//优先级队列表示为一个平衡的二叉堆:queue[n]的两个子节点是queue[2*n+1]和queue[2*(n+1)]。
//优先级队列按比较器排序，如果比较器为空，则按元素的自然顺序排序
//对于堆中的每个节点n和n的每个后代d, n <= d。最低值的元素在队列[0]中(假设队列非空)。
transient Object[] queue; // non-private to simplify nested class access
//优先级队列的元素个数
private int size = 0;
//堆节点排序的比较器，为空的话按自然顺序排序
private final Comparator<? super E> comparator;
//记录当前集合被修改的次数，迭代器遍历的过程中内容发生变化会导致迭代器抛出 ConcurrentModificationException
//即 fail-fast (快速失败)
transient int modCount = 0; // non-private to simplify nested class access
```

> fail-fast(快速失败) 和 fail-safe(安全失败)一直没理解名字是怎么来的，跟实现原理感觉没啥关系。
>
> fail-fast: 在使用迭代器遍历一个集合对象时，如果遍历过程中对集合对象的内容进行了修改(增删改),会抛出 ConcurrentModificationException 异常。
>
> fail-safe: 在遍历时不是直接在集合内容上访问的,而是先copy原有集合内容,在拷贝的集合上进行遍历，在遍历过程中对原集合所作的修改并不能被迭代器检测到,所以不会出发ConcurrentModificationException。
>
> java.util包下的集合类都是快速失败机制的，不能在多线程下发生并发修改；
>
> java.util.concurrent包下的容器都是安全失败的，可以在多线程下并发使用，并发修改。

### PriorityBlockingQueue

线程安全的优先级阻塞队列。

```java
//初始容量: 11 
private static final int DEFAULT_INITIAL_CAPACITY = 11;
//最大容量，超过这个容量后 throw new OutOfMemoryError();
private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
//最小堆，初始化：this.queue = new Object[initialCapacity];
private transient Object[] queue;
//优先级队列的元素个数
private transient int size;
private transient Comparator<? super E> comparator;
//保证
private final ReentrantLock lock;
//非空条件，PriorityBlockingQueue 只会出队阻塞（队列为空时阻塞），入队不会阻塞
private final Condition notEmpty;
//用于防止多线程同时扩容的CAS自旋锁
//参考: tryGrow() 方法
//if (allocationSpinLock == 0 && UNSAFE.compareAndSwapInt(this, allocationSpinLockOffset, 0, 1)) {...}
private transient volatile int allocationSpinLock;
//用于序列化，当需要对PriorityBlockingQueue序列化时，就拷贝队列数据到PriorityQueue<E> q, 然后写到对象输出流ObjectOutputStream　 
private PriorityQueue<E> q;
```

线程安全实现原理：

在入队（put add offer）出队（poll take）、peek、删除、是否包含等操作方法中通过ReentrantLock锁控制操作同步执行。

入队操作（最小堆插入元素）:
设父亲节点索引`n`，则两个子节点索引为`2n+1`、`2n+2`。

```java
private static <T> void siftUpComparable(int k, T x, Object[] array) {	//siftUpUsingComparator同理
    Comparable<? super T> key = (Comparable<? super T>) x;
    //从堆低最后一个元素开始往堆顶方向找父节点并比较，小于父节点就交换，直到大于父节点，或到达堆顶
    while (k > 0) {
        int parent = (k - 1) >>> 1;		
        Object e = array[parent];
        if (key.compareTo((T) e) >= 0)		//if (cmp.compare(x, (T) e) >= 0)
            break;
        array[k] = e;
        k = parent;
    }
    array[k] = key;
}
```

阻塞的实现原理：｀while循环+Condition条件等待｀

```java
//take()
while ( (result = dequeue()) == null)
                notEmpty.await();
//poll(long timeout, TimeUnit unit)
while ( (result = dequeue()) == null && nanos > 0)
                nanos = notEmpty.awaitNanos(nanos);
//offer()
//元素插入最小堆后执行条件唤醒
notEmpty.signal();
```

### DelayQueue

延迟队列。

特点：

+ 基于优先级队列
+ 无界队列，入队不阻塞，出队没有元素的话会阻塞，元素没到期也会阻塞
+ 使用ReentrantLock保证并发安全
+ 任务处理采用Leader-Follower多线程模式

```java
private final transient ReentrantLock lock = new ReentrantLock();
//基于组合优先级队列PriorityQueue实现，
//由于DelayQueue中遍历器使用fail-safe重新实现，遍历的同时修改值并不会抛ConcurrentModificationException
private final PriorityQueue<E> q = new PriorityQueue<E>();
/**
     * Thread designated to wait for the element at the head of
     * the queue.  This variant of the Leader-Follower pattern
     * (http://www.cs.wustl.edu/~schmidt/POSA/POSA2/) serves to
     * minimize unnecessary timed waiting.  When a thread becomes
     * the leader, it waits only for the next delay to elapse, but
     * other threads await indefinitely.  The leader thread must
     * signal some other thread before returning from take() or
     * poll(...), unless some other thread becomes leader in the
     * interim.  Whenever the head of the queue is replaced with
     * an element with an earlier expiration time, the leader
     * field is invalidated by being reset to null, and some
     * waiting thread, but not necessarily the current leader, is
     * signalled.  So waiting threads must be prepared to acquire
     * and lose leadership while waiting.
     */
//Leader-Follower多线程模式带等待时间的变种
//Leader-Follower多线程模式参考讲义：http://www.cs.wustl.edu/~schmidt/POSA/POSA2/
private Thread leader = null;
/**
     * Condition signalled when a newer element becomes available
     * at the head of the queue or a new thread may need to
     * become leader.
     */
private final Condition available = lock.newCondition();
```

延迟获取节点

```java
public E take() throws InterruptedException {
    final ReentrantLock lock = this.lock;
    lock.lockInterruptibly();
    try {
        for (;;) {
            // 堆顶元素
            E first = q.peek();
            // 如果堆顶元素为空，说明队列中还没有元素，直接阻塞等待
            if (first == null)
                available.await();
            else {
                // 堆顶元素的到期时间
                long delay = first.getDelay(NANOSECONDS);
                // 如果小于0说明已到期，直接调用poll()方法弹出堆顶元素
                if (delay <= 0)
                    return q.poll();
                
                // 如果delay大于0 ，则下面要阻塞了
                
                // 将first置为空方便gc，因为有可能其它元素弹出了这个元素
                // 这里还持有着引用不会被清理
                first = null; // don't retain ref while waiting
                // 如果前面有其它线程在等待，直接进入等待
                if (leader != null)
                    available.await();
                else {
                    // 如果leader为null，把当前线程赋值给它
                    Thread thisThread = Thread.currentThread();
                    leader = thisThread;
                    try {
                        // 等待delay时间后自动醒过来
                        // 醒过来后把leader置空并重新进入循环判断堆顶元素是否到期
                        // 这里即使醒过来后也不一定能获取到元素
                        // 因为有可能其它线程先一步获取了锁并弹出了堆顶元素
                        // 条件锁的唤醒分成两步，先从Condition的队列里出队
                        // 再入队到AQS的队列中，当其它线程调用LockSupport.unpark(t)的时候才会真正唤醒
                        available.awaitNanos(delay);
                    } finally {
                        // 如果leader还是当前线程就把它置为空，让其它线程有机会获取元素
                        if (leader == thisThread)
                            leader = null;
                    }
                }
            }
        }
    } finally {
        // 成功出队后，如果leader为空且堆顶还有元素，就唤醒下一个等待的线程
        if (leader == null && q.peek() != null)
            // signal()只是把等待的线程放到AQS的队列里面，并不是真正的唤醒
            available.signal();
        // 解锁，这才是真正的唤醒
        lock.unlock();
    }
}
```

### SynchronousQueue

```java

```



## 并发Map

### ConcurrentHashMap

JDK1.5-1.7的实现(分段锁):

并不是拓展HashMap实现的，而是类似HashMap，额外封装了分段锁。

就好像是一个个小的HashMap（默认16个段）,分别用一个ReentrantLock进行线程同步。由于锁的粒度比较细，ConcurrentHashMap的性能相较hashtable才表现的优异，如果只使用一把锁控制全部的数据读写，性能就很差了。

JDK1.8的实现(CAS和synchronized):

数据结构和HashMap一样，使用CAS和synchronized实现线程安全。

因为HashMap的并发问题主要体现在对同一个Hash索引位置的读写（想想也确实是，两个线程分别读写不同Hash索引位置根本不互相影响），那么我们只需要把锁加在要操作的索引位置就行了，也就是链表表头或者红黑树的首节点；这么实现锁的粒度比分段锁细多了，性能非常好。



## 读写锁集合

读写锁对于读读不互斥，写与读写都互斥。

本以为是基于`ReentrantReadWriteLock`实现，但是实际不是的，而是采取“**只对写操作加`ReentrantLock`, 写过程中会新复制一个容器, 将新元素加进去之后，再将新的容器赋值给引用对象**”。

可以解决多线程并发问题，但是却有一倍内存的额外消耗（只适合读远多于写的场景，写如果多的话，会有大量内存分配与回收操作），而且是保证数据最终一致性而不是实时一致性。

### CopyOnWriteArrayList

实现原理:

元素通过`Object[] array`存储，内部维持一个`ReentrantLock lock`;

只对写操作加锁，写过程中先深复制一个`array`，然后将元素添加到新的`array`，再将引用指向这个新的`array`，读操作直接从array通过索引读取。

### CopyOnWriteArraySet

实现原理:

基于`CopyOnWriteArrayList`实现，使用的`CopyOnWriteArrayList`的`addIfAbsent()`方法添加元素来确保集合没有重复元素。