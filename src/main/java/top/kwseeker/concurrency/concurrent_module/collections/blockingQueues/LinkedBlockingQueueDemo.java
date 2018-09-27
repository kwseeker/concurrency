package top.kwseeker.concurrency.concurrent_module.collections.blockingQueues;

/**
 * BlockingQueue(Interface)
 *
 *      SynchronousQueue            //==================================================================================
 *
 *      PriorityBlockingQueue       //==================================================================================
 *
 *      LinkedBlockingQueue         //==================================================================================
 *          extends AbstractQueue<E>
 *          implements BlockingQueue<E>, java.io.Serializable
 *
 *          和ArrayBlockingQueue数据结构上不同，以及入列出列的算法上的差异;
 *
 *          还有LinkedBlockQueue用了两把锁，一个用于出列一个用于入列；ArrayBlockingQueue只用了一把锁。
 *          TODO: 感觉这个问题里面有料：为什么Java源码作者在ArrayBlockingQueue上不使用两把锁？
 *
 *          1）数据结构
 *              存储元素个数为 AtomicInteger count ,头尾节点为 Node<E> head, Node<E> last， 容量为 int capacity 的链表;
 *              带着可重入锁 ReentrantLock putLock, ReentrantLock takeLock, 条件变量Condition notEmpty, Condition notFull用于指示队列是否未满或非空;
 *              以及一个迭代器 Itrs itrs 。
 *          2）公有方法
 *              和ArrayBlockingQueue基本一致
 *          3）队列入列出列实现： dequeue()和enqueue()的实现
 *              takeIndex指要出列的数组元素index, putIndex指要出列的元素的index.
 *              入列一个元素后 putIndex++,直到达到最大，然后putIndex从0重新开始；
 *              出列一个元素后 takeIndex++, 直到达到最大，然后takeIndex从0重新开始。
 *
 *      BlockingDeque(Interface)    //==================================================================================
 *          LinkedBlockingDeque
 *
 *      ArrayBlockingQueue          //==================================================================================
 *
 *      TransferQueue               //==================================================================================
 *          LinkedTransferQueue
 *
 *      DelayQueue                  //==================================================================================
 *
 */
public class LinkedBlockingQueueDemo {
}
