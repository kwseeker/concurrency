package top.kwseeker.concurrency.concurrent_module.collections.blockingQueues;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * BlockingQueue(Interface)
 *
 *      SynchronousQueue            //==================================================================================
 *
 *      PriorityBlockingQueue       //==================================================================================
 *
 *      LinkedBlockingQueue         //==================================================================================
 *
 *      BlockingDeque(Interface)    //==================================================================================
 *          LinkedBlockingDeque
 *
 *      ArrayBlockingQueue          //==================================================================================
 *          extends AbstractQueue<E>
 *          implements BlockingQueue<E>, java.io.Serializable
 *          1）数据结构
 *              存储元素个数为 int count ,带着存取索引 takeIndex, putIndex, 对象为Object[] items 的数组;
 *              带着可重入锁 ReentrantLock lock, 条件变量Condition notEmpty, Condition notFull用于指示队列是否未满或非空;
 *              以及一个迭代器 Itrs itrs 。
 *          2）公有方法
 *              入列：
 *                  add() addAll() 非线程安全的
 *                  offer() 线程安全非阻塞方法
 *                  put() 线程安全的阻塞方法
 *              出列：
 *                  poll() 线程安全的非阻塞方法
 *                  take() 线程安全的阻塞方法
 *              删：
 *                  clear() 线程安全的方法，
 *                  drainTO() 从ArrayBlockingQueue将数据转移到目标容器类对象
 *              查：
 *                  contains() containsAll()
 *                  peek() 获取将要出列的元素
 *                  remainingCapacity() 剩余的空间
 *                  size() 队列元素个数
 *              其他：
 *                  iterator()  线程安全的迭代器
 *          3）队列入列出列实现： dequeue()和enqueue()的实现
 *              takeIndex指要出列的数组元素index, putIndex指要出列的元素的index.
 *              入列一个元素后 putIndex++,直到达到最大，然后putIndex从0重新开始；
 *              出列一个元素后 takeIndex++, 直到达到最大，然后takeIndex从0重新开始。
 *
 *      TransferQueue               //==================================================================================
 *          LinkedTransferQueue
 *
 *      DelayQueue                  //==================================================================================
 *
 */
@Slf4j
public class ArrayBlockingQueueDemo {

    public static void main(String[] args) {

        //=======================================================
        //构造
        //ArrayBlockingQueue<String> arrayBlockingQueue = new ArrayBlockingQueue<>(8);
        ArrayBlockingQueue<String> arrayBlockingQueue1 = new ArrayBlockingQueue<>(4, true);                 //fair体现在先请求的先插入
        ArrayList<String> arrayList = new ArrayList<>();
        String str = "abcdefghigklmn";
        for (int i = 0; i < 8; i++) {
//        for (int i = 0; i < 9; i++) {     //arrayList的数据超出ArrayBlockingQueue流量插入会报异常 IllegalArgumentException
            arrayList.add(String.valueOf(str.charAt(i)));
        }
        ArrayBlockingQueue<String> arrayBlockingQueue2 = new ArrayBlockingQueue<>(8, true, arrayList);      //从Collection对象初始化
//        arrayBlockingQueue2.add("h");       //已满之后继续插入，也是报告IllegalArgumentException


        //简单的查看方法直接在Debug模式的Evaluate窗口尝试即可，就像执行shell脚本函数一样
        ArrayBlockingQueue<String> arrayBlockingQueueTemp = new ArrayBlockingQueue<>(8);
        arrayBlockingQueueTemp.addAll(arrayList);
//        arrayBlockingQueueTemp.contains("a");
//        arrayBlockingQueueTemp.clear();
//        arrayBlockingQueueTemp.containsAll(arrayList);
        arrayBlockingQueue1.add("A");
        ArrayList<String> arrayList1 = new ArrayList<>();
        arrayBlockingQueue1.drainTo(arrayList1);            //将数据从ArrayBlockingQueue中移动到目标 Collection<? super E> 对象，自身会丢失数据

        Iterator<String> iterator = arrayBlockingQueue2.iterator();
        while(iterator.hasNext()) {
            String element = iterator.next();
            log.info("{}", element);
        }

        //插入的三种方法与区别， add() 为非线程安全的方法， offer()是非阻塞的线程安全的方法， put()是阻塞的线程安全的方法
        //队列已满时插入，add()会报异常，offer()会返回false表示插入失败，put()则是阻塞等待直到有人发信号说未满
        arrayBlockingQueue1.offer("A");
        arrayBlockingQueue1.offer("A");
        arrayBlockingQueue1.offer("A");
        //插满之后
        arrayBlockingQueue1.offer("B");                 //插入数据到队列末尾

        new Thread(()->{
            try {
                log.info("线程将要阻塞");
                arrayBlockingQueue1.put("C");           //线程阻塞等待, put()判断队列是否已满，已满的话执行 notFull.await()等待队列未满信号
                log.info("阻塞线程继续执行");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(()->{
            log.info("出列一个元素，并唤醒阻塞线程执行插入任务");
            arrayBlockingQueue1.poll();                     //将一个元素出列，poll() 会调用 私有方法dequeue()发送信号notFull.signal()， poll是非阻塞的出列;
        }).start();
//        arrayBlockingQueue1.add("D");                     //这个方法是不推荐使用的

        log.info("To take element: {}", arrayBlockingQueue1.peek());   //这个函数说它自己： "就是看看，不拿"， 查看下一个将要出栈的元素
        try {
            arrayBlockingQueue1.take();                                 //出列一个元素，是阻塞形式的出列
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log.info("Element count: {}", arrayBlockingQueue1.size());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
