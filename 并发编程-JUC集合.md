

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

#### JDK1.8ConcurrentHashMap实现原理：

##### 数据结构

```java
//常量--------------------------------------------------------------------------------------------------
//最大容量
private static final int MAXIMUM_CAPACITY = 1 << 30;
//默认初始容量
private static final int DEFAULT_CAPACITY = 16;
//数组（哈希表/哈希桶）的最大size
//这里可以看到数组的最大size大于MAXIMUM_CAPACITY, 并不奇怪因为有的数组位置可能为null
//用于 toArray 以及相关的方法
static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
//用于兼容JDK旧版本实现（默认并发等级也就是分成多少个单独上锁的区域），JDK1.8中只看到在序列化方法中有用先不管
private static final int DEFAULT_CONCURRENCY_LEVEL = 16;
//扩容因子
private static final float LOAD_FACTOR = 0.75f;
//hash冲突节点链表转红黑树的size阈值
static final int TREEIFY_THRESHOLD = 8;
//红黑树转链表的size阈值，是的冲突的节点少了的话还可以转回链表, 参考 untreeify() 方法
static final int UNTREEIFY_THRESHOLD = 6;
/**
     * The smallest table capacity for which bins may be treeified.
     * (Otherwise the table is resized if too many nodes in a bin.)
     * The value should be at least 4 * TREEIFY_THRESHOLD to avoid
     * conflicts between resizing and treeification thresholds.
     */
//将哈希表某个节点的链表转换成红黑树的最小的容量；
//即如果哈希表某个节点处的链表的长度达到TREEIFY_THRESHOLD，但是哈希表的长度没有达到MIN_TREEIFY_CAPACITY，不会将链表转成红黑树，
//而是优先扩容，进行数据迁移；
//treeifyBin()方法中使用, Bin这里的意思是容器
static final int MIN_TREEIFY_CAPACITY = 64;
//给各扩容线程分配迁移任务的数据段最小步长（最小16个哈希表节点）
private static final int MIN_TRANSFER_STRIDE = 16;
/**
     * The number of bits used for generation stamp in sizeCtl.
     * Must be at least 6 for 32bit arrays.
     */
//这个相当于分界线，低$RESIZE_STAMP_BITS位, 存储有多少协助扩容的线程
private static int RESIZE_STAMP_BITS = 16;
//最大的可同时帮助扩容的线程的数量，默认１<<16-1 = 65535 个
private static final int MAX_RESIZERS = (1 << (32 - RESIZE_STAMP_BITS)) - 1;
/**
     * The bit shift for recording size stamp in sizeCtl.
     */
private static final int RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;
//节点内部hash字段的值，-1表示当前哈希表位置的数据正在扩容
static final int MOVED     = -1; // hash for forwarding nodes
//节点内部hash字段的值，-2表示当前哈希表位置下挂载的是一个红黑树
static final int TREEBIN   = -2; // hash for roots of trees
//当前hash位置被预留
static final int RESERVED  = -3; // hash for transient reservations
//保留hash的bit位（后31位）
static final int HASH_BITS = 0x7fffffff; // usable bits of normal node hash
static final int NCPU = Runtime.getRuntime().availableProcessors();

//变量--------------------------------------------------------------------------------------------------
//存放数据的数组（哈希桶）
transient volatile Node<K,V>[] table;
//扩容后的新哈希表
private transient volatile Node<K,V>[] nextTable;
//baseCount是元素个数的基本计数器，加上CounterCell[]中的值，才是最终实际的元素个数，和LongAddr一个原理
private transient volatile long baseCount;
//table size 控制标志: 
//初始化table时 sizeCtl是指定的初始容量(一旦开始初始化table, 改为-1, 初始化完成后改为下次可resize的容量大小)；
//未指定初始容量则sizeCtl=0 (默认值)
// -1:正在初始化table、-(1+activeResizingThreadsNum): 正在resize table
private transient volatile int sizeCtl;
//扩容时下一个待迁移区块的开始索引值
private transient volatile int transferIndex;
//用于扩容或创建CounterCells的自旋锁（循环＋cas对cellsBusy设值）
private transient volatile int cellsBusy;
//计数器拆分出来的一堆CounterCell（原理和LongAddr一样）, 它们的和是计数器的值，cell的size是２的幂指数
private transient volatile CounterCell[] counterCells;
// views
private transient KeySetView<K,V> keySet;
private transient ValuesView<K,V> values;
private transient EntrySetView<K,V> entrySet;
```

##### 插入操作

**putVal()** : 

```java
final V putVal(K key, V value, boolean onlyIfAbsent) {
    if (key == null || value == null) throw new NullPointerException();
    //先计算key的hashCode, 返回值是int类型，然后和高16位按位与，取结果的低31位（丢弃符号位）
    int hash = spread(key.hashCode());
    int binCount = 0;
    for (Node<K,V>[] tab = table;;) {	//这个for是复用的
        Node<K,V> f; int n, i, fh;
        if (tab == null || (n = tab.length) == 0)	//哈希表为空，则进行初始化
            //通过 while + cas 自旋锁实现建表的并发安全
            tab = initTable();
        else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {	//使用Unsafe getObjectVolatile() 方法获取数组元素的最新值，带volatile load性质
            //如果这位置上元素为空，结合上面的for + 这里的cas组成自旋锁并发安全的put值
            if (casTabAt(tab, i, null, new Node<K,V>(hash, key, value, null)))
                break;                   // no lock when adding to empty bin
        }
        else if ((fh = f.hash) == MOVED)
            //
            tab = helpTransfer(tab, f);
        else {
            //其他情况：索引位置有值且未因扩容在迁移
            //这时有２种情况：
            //1) 目标位置是链表
            //2) 目标位置是红黑树
            V oldVal = null;
            synchronized (f) {	
                // 如果对应的下标位置 的节点没有改变，如果节点改变，说明期间有其他线程修改了
                if (tabAt(tab, i) == f) {
                    if (fh >= 0) {										//fh>=0那么哈希表目标位置肯定是链表（如果是红黑树fh==-2）
                        binCount = 1;
                        for (Node<K,V> e = f;; ++binCount) {
                            K ek;
                            if (e.hash == hash &&
                                ((ek = e.key) == key ||
                                 (ek != null && key.equals(ek)))) {
                                oldVal = e.val;
                                if (!onlyIfAbsent)
                                    e.val = value;
                                break;
                            }
                            Node<K,V> pred = e;
                            if ((e = e.next) == null) {
                                pred.next = new Node<K,V>(hash, key,
                                                          value, null);
                                break;
                            }
                        }
                    }
                    else if (f instanceof TreeBin) {	//哈希表目标位置是红黑树
                        Node<K,V> p;
                        binCount = 2;
                        if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,
                                                              value)) != null) {
                            oldVal = p.val;
                            if (!onlyIfAbsent)
                                p.val = value;
                        }
                    }
                }
            } //end synchronized
            if (binCount != 0) {
                if (binCount >= TREEIFY_THRESHOLD)
                    treeifyBin(tab, i);		//如果链表数量超过TREEIFY_THRESHOLD（８），转成红黑树
                if (oldVal != null)
                    return oldVal;
                break;
            }
        }
    }
    addCount(1L, binCount);		//非替换情况下，put成功后，元素个数计数器加１
    return null;
}
```

**hash算法**：

先计算key的hashCode, 返回值是int类型，然后和高16位按位与，取结果的低31位（丢弃符号位）

```java
int hash = spread(key.hashCode());
static final int spread(int h) {
	return (h ^ (h >>> 16)) & HASH_BITS;	//HASH_BITS: 0x7fffffff, 相当于做了下mask,只取低31位
}
```

**哈希表的初始化**：

通过 `while + cas `自旋锁实现建表的并发安全，拿锁成功后又检查了一次表是否存在（结合外边的检查，其实就是双重检查）

**取出数组中某索引位置最新的数据**：

使用Unsafe getObjectVolatile() 方法获取数组元素的最新值。

```java
//这个volatile只能保证table引用每次读取都是最新的，无法确保内部元素读取也是最新的
transient volatile Node<K,V>[] table;

static final <K,V> Node<K,V> tabAt(Node<K,V>[] tab, int i) {
    //Node指针占４字节，数组类型的对象头（64位系统开启指针压缩：8字节Markword+4字节元数据指针[KClass指针]+4字节数组长度）
    return (Node<K,V>)U.getObjectVolatile(tab, ((long)i << ASHIFT) + ABASE);
}
```

##### 扩容操作

**扩容的几个触发条件**：

1) put() 方法中，链表中元素个数 >= TREEIFY_THRESHOLD 且 哈希表长度 tab.length < MIN_TREEIFY_CAPACITY 时扩容

```java
if (binCount >= TREEIFY_THRESHOLD)			//链表中元素个数 >= TREEIFY_THRESHOLD
                        treeifyBin(tab, i);

private final void treeifyBin(Node<K,V>[] tab, int index) {
	...
	if ((n = tab.length) < MIN_TREEIFY_CAPACITY)	//哈希表长度 tab.length < MIN_TREEIFY_CAPACITY
                tryPresize(n << 1);
	...
}
```

2) addCount() 方法中，元素个数达到扩容阈值

参考后面计数器实现分析（调用`transfer()`，将旧哈希表的数据拷贝到新的哈希表）

3) putAll() 目标map存不下原map的数据时扩容

```java
public void putAll(Map<? extends K, ? extends V> m) {
    tryPresize(m.size());
    for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
        putVal(e.getKey(), e.getValue(), false);
}
```

**扩容的原理**：

```java
private final void tryPresize(int size) {
    int c = (size >= (MAXIMUM_CAPACITY >>> 1)) ? MAXIMUM_CAPACITY :
    tableSizeFor(size + (size >>> 1) + 1);
    int sc;
    
    while ((sc = sizeCtl) >= 0) {
        Node<K,V>[] tab = table; int n;
        if (tab == null || (n = tab.length) == 0) {
            n = (sc > c) ? sc : c;
            if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
                try {
                    if (table == tab) {
                        @SuppressWarnings("unchecked")
                        Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                        table = nt;
                        sc = n - (n >>> 2);
                    }
                } finally {
                    sizeCtl = sc;
                }
            }
        }
        else if (c <= sc || n >= MAXIMUM_CAPACITY)
            break;
        else if (tab == table) {
            int rs = resizeStamp(n);
            if (sc < 0) {
                Node<K,V>[] nt;
                if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
                    sc == rs + MAX_RESIZERS || (nt = nextTable) == null ||
                    transferIndex <= 0)
                    break;
                if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1))
                    transfer(tab, nt);
            }
            else if (U.compareAndSwapInt(this, SIZECTL, sc,
                                         (rs << RESIZE_STAMP_SHIFT) + 2))
                //数据迁移
                transfer(tab, null);
        }
    }
}

//总的来说就是：充分利用正在扩容的所有线程一起做迁移，每个线程领取一段步长的数据迁移到新表，迁移完一段再看看处理到哪一段了
//然后继续领取一段待迁移数据执行迁移，直到所有数据都迁移完毕
//下面注释来源于：https://www.jianshu.com/p/2f710b952c14
private final void transfer(Node<K,V>[] tab, Node<K,V>[] nextTab) {
    int n = tab.length, stride;
    // 将 length / 8 然后除以 CPU核心数。如果得到的结果小于 16，那么就使用 16。
    // 这里的目的是让每个 CPU 处理的桶一样多，避免出现转移任务不均匀的现象，如果桶较少的话，默认一个 CPU（一个线程）处理 16 个桶
    if ((stride = (NCPU > 1) ? (n >>> 3) / NCPU : n) < MIN_TRANSFER_STRIDE)
        stride = MIN_TRANSFER_STRIDE; // subdivide range 细分范围 stridea：TODO
    // 新的 table 尚未初始化
    if (nextTab == null) {            // initiating
        try {
            // 扩容  2 倍
            Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n << 1];
            // 更新
            nextTab = nt;
        } catch (Throwable ex) {      // try to cope with OOME
            // 扩容失败， sizeCtl 使用 int 最大值。
            sizeCtl = Integer.MAX_VALUE;
            return;// 结束
        }
        // 更新成员变量
        nextTable = nextTab;
        // 更新转移下标，就是 老的 tab 的 length
        transferIndex = n;
    }
    // 新 tab 的 length
    int nextn = nextTab.length;
    // 创建一个 fwd 节点，用于占位。当别的线程发现这个槽位中是 fwd 类型的节点，则跳过这个节点。
    ForwardingNode<K,V> fwd = new ForwardingNode<K,V>(nextTab);
    // 首次推进为 true，如果等于 true，说明需要再次推进一个下标（i--），反之，如果是 false，那么就不能推进下标，需要将当前的下标处理完毕才能继续推进
    boolean advance = true;
    // 完成状态，如果是 true，就结束此方法。
    boolean finishing = false; // to ensure sweep before committing nextTab
    // 死循环,i 表示下标，bound 表示当前线程可以处理的当前桶区间最小下标
    for (int i = 0, bound = 0;;) {
        Node<K,V> f; int fh;
        // 如果当前线程可以向后推进；这个循环就是控制 i 递减。同时，每个线程都会进入这里取得自己需要转移的桶的区间
        while (advance) {
            int nextIndex, nextBound;
            // 对 i 减一，判断是否大于等于 bound （正常情况下，如果大于 bound 不成立，说明该线程上次领取的任务已经完成了。那么，需要在下面继续领取任务）
            // 如果对 i 减一大于等于 bound（还需要继续做任务），或者完成了，修改推进状态为 false，不能推进了。任务成功后修改推进状态为 true。
            // 通常，第一次进入循环，i-- 这个判断会无法通过，从而走下面的 nextIndex 赋值操作（获取最新的转移下标）。其余情况都是：如果可以推进，将 i 减一，然后修改成不可推进。如果 i 对应的桶处理成功了，改成可以推进。
            if (--i >= bound || finishing)
                advance = false;// 这里设置 false，是为了防止在没有成功处理一个桶的情况下却进行了推进
            // 这里的目的是：1. 当一个线程进入时，会选取最新的转移下标。2. 当一个线程处理完自己的区间时，如果还有剩余区间的没有别的线程处理。再次获取区间。
            else if ((nextIndex = transferIndex) <= 0) {
                // 如果小于等于0，说明没有区间了 ，i 改成 -1，推进状态变成 false，不再推进，表示，扩容结束了，当前线程可以退出了
                // 这个 -1 会在下面的 if 块里判断，从而进入完成状态判断
                i = -1;
                advance = false;// 这里设置 false，是为了防止在没有成功处理一个桶的情况下却进行了推进
            }// CAS 修改 transferIndex，即 length - 区间值，留下剩余的区间值供后面的线程使用
            else if (U.compareAndSwapInt
                     (this, TRANSFERINDEX, nextIndex,
                      nextBound = (nextIndex > stride ?
                                   nextIndex - stride : 0))) {
                bound = nextBound;// 这个值就是当前线程可以处理的最小当前区间最小下标
                i = nextIndex - 1; // 初次对i 赋值，这个就是当前线程可以处理的当前区间的最大下标
                advance = false; // 这里设置 false，是为了防止在没有成功处理一个桶的情况下却进行了推进，这样对导致漏掉某个桶。下面的 if (tabAt(tab, i) == f) 判断会出现这样的情况。
            }
        }// 如果 i 小于0 （不在 tab 下标内，按照上面的判断，领取最后一段区间的线程扩容结束）
        //  如果 i >= tab.length(不知道为什么这么判断)
        //  如果 i + tab.length >= nextTable.length  （不知道为什么这么判断）
        if (i < 0 || i >= n || i + n >= nextn) {
            int sc;
            if (finishing) { // 如果完成了扩容
                nextTable = null;// 删除成员变量
                table = nextTab;// 更新 table
                sizeCtl = (n << 1) - (n >>> 1); // 更新阈值
                return;// 结束方法。
            }// 如果没完成
            if (U.compareAndSwapInt(this, SIZECTL, sc = sizeCtl, sc - 1)) {// 尝试将 sc -1. 表示这个线程结束帮助扩容了，将 sc 的低 16 位减一。
                if ((sc - 2) != resizeStamp(n) << RESIZE_STAMP_SHIFT)// 如果 sc - 2 不等于标识符左移 16 位。如果他们相等了，说明没有线程在帮助他们扩容了。也就是说，扩容结束了。
                    return;// 不相等，说明没结束，当前线程结束方法。
                finishing = advance = true;// 如果相等，扩容结束了，更新 finising 变量
                i = n; // 再次循环检查一下整张表
            }
        }
        else if ((f = tabAt(tab, i)) == null) // 获取老 tab i 下标位置的变量，如果是 null，就使用 fwd 占位。
            advance = casTabAt(tab, i, null, fwd);// 如果成功写入 fwd 占位，再次推进一个下标
        else if ((fh = f.hash) == MOVED)// 如果不是 null 且 hash 值是 MOVED。
            advance = true; // already processed // 说明别的线程已经处理过了，再次推进一个下标
        else {// 到这里，说明这个位置有实际值了，且不是占位符。对这个节点上锁。为什么上锁，防止 putVal 的时候向链表插入数据
            synchronized (f) {
                // 判断 i 下标处的桶节点是否和 f 相同
                if (tabAt(tab, i) == f) {
                    Node<K,V> ln, hn;// low, height 高位桶，低位桶
                    
                    // 如果是链表;  如果 f 的 hash 值大于 0 。TreeBin 的 hash 是 -2
                    if (fh >= 0) {
                        // 对老长度进行与运算（第一个操作数的的第n位于第二个操作数的第n位如果都是1，那么结果的第n为也为1，否则为0）
                        // 由于 Map 的长度都是 2 的次方（000001000 这类的数字），那么取于 length 只有 2 种结果，一种是 0，一种是1
                        //  如果是结果是0 ，Doug Lea 将其放在低位，反之放在高位，目的是将链表重新 hash，放到对应的位置上，让新的取于算法能够击中他。
                        int runBit = fh & n;
                        Node<K,V> lastRun = f; // 尾节点，且和头节点的 hash 值取于不相等
                        // 遍历这个桶
                        for (Node<K,V> p = f.next; p != null; p = p.next) {
                            // 取于桶中每个节点的 hash 值
                            int b = p.hash & n;
                            // 如果节点的 hash 值和首节点的 hash 值取于结果不同
                            if (b != runBit) {
                                runBit = b; // 更新 runBit，用于下面判断 lastRun 该赋值给 ln 还是 hn。
                                lastRun = p; // 这个 lastRun 保证后面的节点与自己的取于值相同，避免后面没有必要的循环
                            }
                        }
                        if (runBit == 0) {// 如果最后更新的 runBit 是 0 ，设置低位节点
                            ln = lastRun;
                            hn = null;
                        }
                        else {
                            hn = lastRun; // 如果最后更新的 runBit 是 1， 设置高位节点
                            ln = null;
                        }// 再次循环，生成两个链表，lastRun 作为停止条件，这样就是避免无谓的循环（lastRun 后面都是相同的取于结果）
                        for (Node<K,V> p = f; p != lastRun; p = p.next) {
                            int ph = p.hash; K pk = p.key; V pv = p.val;
                            // 如果与运算结果是 0，那么就还在低位
                            if ((ph & n) == 0) // 如果是0 ，那么创建低位节点
                                ln = new Node<K,V>(ph, pk, pv, ln);
                            else // 1 则创建高位
                                hn = new Node<K,V>(ph, pk, pv, hn);
                        }
                        // 其实这里类似 hashMap 
                        // 设置低位链表放在新链表的 i
                        setTabAt(nextTab, i, ln);
                        // 设置高位链表，在原有长度上加 n
                        setTabAt(nextTab, i + n, hn);
                        // 将旧的链表设置成占位符
                        setTabAt(tab, i, fwd);
                        // 继续向后推进
                        advance = true;
                    }
                    
                    // 如果是红黑树
                    else if (f instanceof TreeBin) {
                        TreeBin<K,V> t = (TreeBin<K,V>)f;
                        TreeNode<K,V> lo = null, loTail = null;
                        TreeNode<K,V> hi = null, hiTail = null;
                        int lc = 0, hc = 0;
                        // 遍历
                        for (Node<K,V> e = t.first; e != null; e = e.next) {
                            int h = e.hash;
                            TreeNode<K,V> p = new TreeNode<K,V>
                                (h, e.key, e.val, null, null);
                            // 和链表相同的判断，与运算 == 0 的放在低位
                            if ((h & n) == 0) {
                                if ((p.prev = loTail) == null)
                                    lo = p;
                                else
                                    loTail.next = p;
                                loTail = p;
                                ++lc;
                            } // 不是 0 的放在高位
                            else {
                                if ((p.prev = hiTail) == null)
                                    hi = p;
                                else
                                    hiTail.next = p;
                                hiTail = p;
                                ++hc;
                            }
                        }
                        // 如果树的节点数小于等于 6，那么转成链表，反之，创建一个新的树
                        ln = (lc <= UNTREEIFY_THRESHOLD) ? untreeify(lo) :
                            (hc != 0) ? new TreeBin<K,V>(lo) : t;
                        hn = (hc <= UNTREEIFY_THRESHOLD) ? untreeify(hi) :
                            (lc != 0) ? new TreeBin<K,V>(hi) : t;
                        // 低位树
                        setTabAt(nextTab, i, ln);
                        // 高位树
                        setTabAt(nextTab, i + n, hn);
                        // 旧的设置成占位符
                        setTabAt(tab, i, fwd);
                        // 继续向后推进
                        advance = true;
                    }
                }
            }
        }
    }
}
```

##### 红黑树操作

##### 计数器实现

和LongAddr的原理一样。不详细看了。

```java
private final void addCount(long x, int check) {
    CounterCell[] as; long b, s;
    if ((as = counterCells) != null ||
        !U.compareAndSwapLong(this, BASECOUNT, b = baseCount, s = b + x)) {
        CounterCell a; long v; int m;
        boolean uncontended = true;
        if (as == null || (m = as.length - 1) < 0 ||
            (a = as[ThreadLocalRandom.getProbe() & m]) == null ||
            !(uncontended =
              U.compareAndSwapLong(a, CELLVALUE, v = a.value, v + x))) {
            fullAddCount(x, uncontended);
            return;
        }
        if (check <= 1)
            return;
        s = sumCount();
    }
    if (check >= 0) {
        Node<K,V>[] tab, nt; int n, sc;
        while (s >= (long)(sc = sizeCtl) && (tab = table) != null &&
               (n = tab.length) < MAXIMUM_CAPACITY) {
            //计算扩容戳：n=1,  10000000 00011111
            int rs = resizeStamp(n);
            if (sc < 0) {
                if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
                    sc == rs + MAX_RESIZERS || (nt = nextTable) == null ||
                    transferIndex <= 0)
                    break;
                if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1))
                    transfer(tab, nt);
            }
            // 10000000 00011111 00000000 00000010
            // 低16位(N+1)表示有N个线程正在扩容
            else if (U.compareAndSwapInt(this, SIZECTL, sc, (rs << RESIZE_STAMP_SHIFT) + 2))
                transfer(tab, null);
            // baseCount + 所有counterCell的值，获取实际元素个数
            s = sumCount();
        }
    }
}
```

##### JDk1.8优化方式总结

**存储结构优化：**

JDK1.7中是数组（哈希桶）+链表实现; JDK1.8中采用数组（哈希桶）+链表+红黑树。当哈希冲突数据较多时，比链表查询效率高很多。

**写数据加锁方式优化：**
JDK1.7中使用分段锁的方式，锁粒度还是比较粗；JDK1.8锁的粒度是数组元素（即链表或红红黑树头节点），粒度大大降低，性能更好。参考前面代码的 `casTabAt()` 和 `synchronized(f){...}`。

**扩容优化：**

JDK1.8添加了辅助扩容，充分利用所有正在扩容的线程分担同一扩容任务，大大提升了扩容时数据迁移的处理效率。

**元素个数计数器优化：**

计数器采用了类似LongAddr的设计，将对一个数据的CAS操作拆分为了对多个数据的CAS操作，减小了并发修改的可能性，从而大大提高了性能。



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