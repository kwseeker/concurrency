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