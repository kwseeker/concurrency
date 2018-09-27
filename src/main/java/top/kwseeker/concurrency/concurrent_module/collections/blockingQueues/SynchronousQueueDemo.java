package top.kwseeker.concurrency.concurrent_module.collections.blockingQueues;

/**
 * BlockingQueue(Interface)
 *
 *      SynchronousQueue            //==================================================================================
 *          extends AbstractQueue<E>
 *          implements BlockingQueue<E>, java.io.Serializable
 *          1）数据结构
 *              同步队列（公平：等待线程根据访问先后顺序加入队列）
 *              公平实现：TransferQueue
 *
 *              非公平实现 TransferStack
 *
 *          2）公有方法
 *
 *          3）队列入列出列实现
 *
 *      PriorityBlockingQueue       //==================================================================================
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
public class SynchronousQueueDemo {

}
