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



## 并发Map集合类

### ConcurrentHashMap

#### JDK1.5-1.7的实现(分段锁)

并不是拓展HashMap实现的，而是类似HashMap，额外封装了分段锁。

就好像是一个个小的HashMap（默认16个段）,分别用一个ReentrantLock进行线程同步。由于锁的粒度比较细，ConcurrentHashMap的性能相较hashtable才表现的优异，如果只使用一把锁控制全部的数据读写，性能就很差了。

#### JDK1.8的实现(CAS和synchronized)

数据结构和HashMap一样，使用CAS和synchronized实现线程安全。

因为HashMap的并发问题主要体现在对同一个Hash索引位置的读写（想想也确实是，两个线程分别读写不同Hash索引位置根本不互相影响），那么我们只需要把锁加在要操作的索引位置就行了，也就是链表表头或者红黑树的首节点；这么实现锁的粒度比分段锁细多了，性能非常好。

## 读写锁集合类

读写锁对于读读不互斥，写与读写都互斥。

本以为是基于`ReentrantReadWriteLock`实现，但是实际不是的，而是采取“**只对写操作加`ReentrantLock`, 写过程中会新复制一个容器, 将新元素加进去之后，再将新的容器赋值给引用对象**”。

可以解决多线程并发问题，但是却有一倍内存的额外消耗（只适合读远多于写的场景，写如果多的话，会有大量内存分配与回收操作），而且是保证数据最终一致性而不是实时一致性。

### CopyOnWriteArrayList

#### 实现原理

元素通过`Object[] array`存储，内部维持一个`ReentrantLock lock`;

只对写操作加锁，写过程中先深复制一个`array`，然后将元素添加到新的`array`，再将引用指向这个新的`array`，读操作直接从array通过索引读取。

### CopyOnWriteArraySet

#### 实现原理

基于`CopyOnWriteArrayList`实现，使用的`CopyOnWriteArrayList`的`addIfAbsent()`方法添加元素来确保集合没有重复元素。