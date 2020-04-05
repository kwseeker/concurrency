# 并发编程-AQS

AQS是一个抽象同步框架，可以用来实现一个依赖线程状态的同步器。JUC中众多的同步器都是通过AQS实现的。

## AQS架构

类图：
```
AbstractQueuedSynchronizer
  -> AbstractOwnableSynchronizer
```

### 锁数据结构

每一个AQS的实现类的实例都是锁对象。

AbstractQueuedSynchronizer
```java
//提供CAS操作支持
unsafe: Unsafe
stateOffset: long
//锁状态，0：锁未被占用，1：锁被占用，>1:锁被重入占用
state: volatile int
//独占线程（对于独占锁的实现会使用到这个成员变量，如ReentrantLock）
exclusiveOwnerThread -> AbstractOwnableSynchronizer  
//等待获取锁的线程节点的等待队列
head: Node
headOffset
tail: Node
tailOffset
nextOffset
spinForTimeoutThreshold
```
Node
```java
waitStatus: volatile int
nextWaiter: Node
prev: Node
next: Node
```
等待状态
```java
/** waitStatus value to indicate thread has cancelled */
static final int CANCELLED =  1;
/** waitStatus value to indicate successor's thread needs unparking */
static final int SIGNAL    = -1;
/** waitStatus value to indicate thread is waiting on condition */
static final int CONDITION = -2;
/**
* waitStatus value to indicate the next acquireShared should
* unconditionally propagate */
static final int PROPAGATE = -3;
```

### AQS锁特性

+ **阻塞等待队列**

+ **共享/独占**

  共享锁如：CountDownLatch、Semaphore
  独占锁如：ReentrantLock

+ **公平/非公平**

  公平锁实现需要先判断队列是否为空，不为空直接创建节点加入到队尾；非公平锁实现先尝试获取锁，失败的话再加入队尾。

+ **可重入**

  通过 state (volatile int) 计数实现，0：锁未被占用，1：锁被占用，>1:锁被重入占用。

+ **允许中断**



## AQS类源码分析

### ReentrantLock

#### 以ReentrantLock的测试开始调试分析

```
public class ReentrantLockTest {
    private int count = 0;
    @Test
    public void testReentrantLock() {
        ReentrantLock lock = new ReentrantLock(); //1
        for (int i = 0; i < 3; i++) {
            new Thread(()->{
                lock.lock();  //2
                count++;
                lock.unlock();  //3
            }).start();
        }
        while (Thread.activeCount() > 2) {}
        System.out.println(count);
    }
}
```

#### **多个线程并发加锁释放锁流程**

  1) 线程A通过Unsafe CAS 比较当前锁对象成员变量state的值是否为0，是的话更新为1。更新成功的话，设置独占线程exclusiveOwnerThread为当前线程。然后执行同步块断点暂停。

  2) 假如又一个线程B加锁执行CAS比较当前锁对象成员变量state的值是否为0，这次肯定失败返回false；执行AbstractQueuedSynchronizer$acquire(int arg)[这个方法在ReentrantLock#NonfairSync中实现]，这个方法内部先读取锁的state值，看看是否为0（即锁有没有释放），释放了则执行第一步的逻辑，没有释放的话，读取当前锁被哪个线程独占，看是不是自己；是自己的话说明是重入加锁，将state值+1；否则返回尝试获取锁失败。

  3) 线程B tryAcquire 失败后，创建新的等待节点[记录当前线程实例到thread成员变量和占锁模式到nextWaiter成员变量，ReentrantLock为独占模式：Node.EXCLUSIVE]加入锁的等待队列。

  4) 线程B会在一个循环中等待，直到头结点释放锁，唤醒所有等待线程。
      ```java
      for (;;) {
        final Node p = node.predecessor();
        if (p == head && tryAcquire(arg)) {
            setHead(node);
            p.next = null; // help GC
            failed = false;
            return interrupted;
        }
        if (shouldParkAfterFailedAcquire(p, node) &&
            parkAndCheckInterrupt())  //这里面调用 LockSupport.park()进入等待
            interrupted = true;
      }
      ```

  5) 线程A执行完同步块代码然后释放锁lock.unlock(), 执行AbstractQueuedSynchronizer$release()；先执行 Sync$tryRelease(),先检查占锁的线程是否为当前线程，然后因为锁是可重入的需要判断state减1是否为0，不为0说明还不能释放锁，这时只会将state值减一。如果tryRelease()执行成功说明真是需要释放锁了，判断head.next节点的waitStatus值，根据值做对应处理。1：直接从队列删除head.next节点；-1：唤醒head.next节点的线程；-2：条件处理。

### Semaphore



### 锁公平性的理解

看源码实现发现之前对**非公平锁**理解有偏差：非公平锁并不是锁释放后，队列中的各个等待线程全部参与竞争，而是队列头部等待线程和其他刚过来请求加锁的线程进行竞争。还有在队列中的线程也不一定按照队列的顺序获取锁，如果中间碰到一个无效的节点（被cancel的节点），则会从尾节点开始往前找。

别人画了张图很形象：
![AQS公平锁和非公平锁区别](./imgs/AQS公平锁和非公平锁区别.png)

### CyclicBarrier

`CyclicBrrier`是同步屏障，用于控制多线程同时开始执行任务。

工作原理：  
基于`Condition`的等待唤醒机制(刚开始猜想是Object wait() notifyAll(),这种应该也是可以实现的)；  
１）设置同步屏障参与的线程数量（作为未准备就绪计数 count）；  
２）前面就绪(count--,表示自己已经就绪)的线程阻塞等待条件(就绪即开始执行任务前调用Condition$await()方法）；  
３）最后一个准备就绪的线程（count--后等于０）执行条件唤醒`Condition$notifyAll()`,唤醒所有线程；同时还需要注意它还会重置count技术和generation标志也既是说可以还可以进行下一轮的“比赛”。  

同时：  
实现中还有一些细节，这里分开说（以往分析源码喜欢将主要逻辑和细节问题放在一起说，陈述罗嗦，让人迷惑，而且对于大框架细节总是探索不玩浪费时间，还影响主要逻辑分析的推进）。  
１）同步计数等值需要使用同步器等措施保证线程安全;  
２）某线程执行异常需要在后置处理中通知其他就绪的线程退出等待；  
３）还有线程被中断的处理、超时处理（比较简单不多加陈述）。  

测试代码:  
`top.kwseeker.concurrency.juclock.CyclicBarrier`。

