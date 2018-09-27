package top.kwseeker.concurrency.concurrent_module.collections.blockingQueues;

/**
 * BlockingQueue(Interface)
 *
 *      SynchronousQueue            //==================================================================================
 *
 *      PriorityBlockingQueue       //==================================================================================
 *          extends AbstractQueue<E>
 *          implements BlockingQueue<E>, java.io.Serializable
 *
 *          PriorityBlockingQueue是带优先级的无界阻塞队列，每次出队都返回优先级最高的元素，
 *          是二叉树最小堆的实现，研究过数组方式存放最小堆节点的都知道，直接遍历队列元素是无序的。
 *
 *          所有插入这个队列的元素必须实现Comparator接口，元素的排序取决于Comparator接口的实现。
 *          获取PriorityBlockingQueue获取Iterator的话，该Iterator并不能保证它对元素的遍历是以优先级为序的。
 *
 *          1）数据结构
 *              初始容量为11 数组最大Integer.MAX_VALUE -8, 元素个数为 size, 对象为Object[] queue 的数组,
 *              附带由一个优先级队列 PriorityQueue<E> q;
 *              有一个比较器 Comparator<? super E> comparator, 一个可重入锁 ReentrantLock lock, 一个自旋锁 allocationSpinLock;
 *              条件变量Condition notEmpty 用于指示队列是否非空。
 *          2）公有方法
 *              相比ArrayBlockingQueue多出了
 *              一个比较器方法： comparator() 用于比较队列元素优先级
 *              一个 spliterator()
 *          3）队列入列出列实现： dequeue()和enqueue()的实现
 *              二叉树最小堆的实现。
 *              为什么堆树适合做优先级队列，因为带优先级队列每次读取都要求优先级最大或最小，
 *              而堆树的堆顶就是最大（最大堆）或最小（最小堆）的。而且取出最大或最小值后，重新到达堆顶的元素
 *              仍然是最大或者最小的。
 *
 *              参考：《并发队列 – 无界阻塞优先级队列 PriorityBlockingQueue 原理探究》 http://www.importnew.com/25541.html
 *
 *      LinkedBlockingQueue         //==================================================================================
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

public class PriorityBlockingQueueDemo {
}
