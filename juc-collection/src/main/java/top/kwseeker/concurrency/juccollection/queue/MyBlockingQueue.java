package top.kwseeker.concurrency.juccollection.queue;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 实现阻塞队列：
 *  要符合阻塞队列规范（实现BlockingQueue接口）；
 *  队列实现采用链表方式；
 *  基于synchronized同步实现线程安全和wait/notify等待唤醒机制；
 *  当队列满继续写阻塞，当队列空继续往外读阻塞。
 *
 * 最后和JDK中的实现对比一下，看JDK的写法好在哪里
 */
public class MyBlockingQueue<E> implements BlockingQueue<E> {

    private Node<E> head;
    private Node<E> tail;
    private int capacity;
    //队列中元素个数计数，count的加减和队列节点的加减必须放在同一个监视器的同步块（可以在多个同步块中但是必须由同一个监视器控制）中。
    private AtomicInteger count = new AtomicInteger(0);

    //这里需要有两个条件，但是synchronized只支持一个条件（监视器对象）
    private final Object notFull = new Object();
    private final Object notEmpty = new Object();

    public MyBlockingQueue() {
        this(Integer.MAX_VALUE);
    }

    public MyBlockingQueue(int capacity) {
        this.capacity = capacity;
    }

    /**
     * 非阻塞插入（队列已满不会等待直接返回）
     *
     * 将指定的元素插入到此队列中（如果立即可行且不会违反容量限制），在成功时返回 true，
     * 如果当前没有可用空间，则抛出 IllegalStateException（即非阻塞的）。
     * 如果 offer 成功，则此实现返回 true，否则抛出 IllegalStateException。
     * @param e
     * @return
     */
    @Override
    public boolean add(E e) {
        if(offer(e)) {
            return true;
        } else {    //队列已满，直接抛出异常
            throw new IllegalStateException("Queue full");
        }
    }

    /**
     * 非阻塞插入
     *
     * 将指定的元素插入此队列（如果立即可行且不会违反容量限制），插值时已满不会阻塞等待
     * 当使用有容量限制的队列时，此方法通常要优于 add(E)，后者可能无法插入元素，而只是抛出一个异常。
     * @param e
     * @return
     */
    @Override
    public boolean offer(E e) {
        //不接受null节点
        if(e == null) {
            throw new NullPointerException("offer() parameter cannot be null");
        }
        if(count.get() == capacity) {      //队列已满，取消插入，返回false
            return false;
        }
        synchronized (notFull) {
            //队列未满，插入新节点，
            if(count.get() == 0) {
                head = tail = new Node<>(e);
            } else if(count.get() < capacity) {
                tail = tail.next = new Node<>(e);
            }
            int oldCount = count.getAndIncrement();
            if(oldCount + 1 < capacity) {       //还是没满
                notFull.notify();
            }
        }
        if(count.get() > 0) {
            synchronized (notEmpty) {
                notEmpty.notify();
            }
        }
        return true;
    }

    /**
     * 阻塞插入，没有超时时间
     * @param e
     * @throws InterruptedException
     */
    @Override
    public void put(E e) throws InterruptedException {
        if(e == null) {
            throw new NullPointerException("offer() parameter cannot be null");
        }
        synchronized (notFull) {
            while(count.get() == capacity) {
                notFull.wait();
            }
            if(count.get() == 0) {
                head = tail = new Node<>(e);
            } else if(count.get() < capacity) {
                tail = tail.next = new Node<>(e);
            }
            System.out.println(Thread.currentThread().getName() + " put value: " + e);
            int oldCount = count.getAndIncrement();
            if(oldCount + 1 < capacity) {
                notFull.notify();
            }
        }
        if(count.get() > 0) {
            synchronized (notEmpty) {
                notEmpty.notify();
            }
        }
    }

    /**
     * 阻塞插入有超时时间
     *
     * offer()的阻塞版本
     * 区别是如果队列已满则wait()阻塞等待
     * @param e
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     */
    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        if(e == null) {
            throw new NullPointerException("offer() parameter cannot be null");
        }

        synchronized (notFull) {
            long waitMs = unit.toMillis(timeout);
            long beginTime = System.currentTimeMillis();
            while(count.get() == capacity) {
                if(System.currentTimeMillis() > beginTime + waitMs) {   //超时退出
                    return false;
                }
                notFull.wait();
            }
            if(count.get() == 0) {
                head = tail = new Node<>(e);
            } else if(count.get() < capacity) {
                tail = tail.next = new Node<>(e);
            }
            int oldCount = count.getAndIncrement();
            if(oldCount + 1 < capacity) {
                notFull.notify();
            }
        }
        if(count.get() > 0) {
            synchronized (notEmpty) {
                notEmpty.notify();
            }
        }
        return true;
    }

    /**
     * 阻塞获取并移除队列头部节点
     *
     * 获取并移除此队列的头部，在元素变得可用之前一直等待（如果有必要）。
     * @return
     * @throws InterruptedException
     */
    @Override
    public E take() throws InterruptedException {
        E ret;
        synchronized (notEmpty) {
            while(count.get() == 0) {
                notEmpty.wait();
            }
            //提取头部节点
            ret = head.item;
            head = head.next;
            System.out.println(Thread.currentThread().getName() + " take value: " + ret);
            //更新计数
            int oldCount = count.getAndDecrement();
            if(oldCount > 1) {
                notEmpty.notify();
            }
        }
        synchronized (notFull) {
            if(count.get() < capacity) {
                notFull.notify();
            }
        }
        return ret;
    }

    /**
     * 不带超时时间的poll()
     * @return
     */
    @Override
    public E poll() {
        return null;
    }

    /**
     * 非阻塞获取并移除队列头部节点
     *
     * 获取并移除此队列的头，如果此队列为空，则返回 null
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     */
    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return null;
    }

    /**
     * 非阻塞移除队列头部节点，如果队列为空返回异常
     *
     * 获取并移除此队列的头。此方法与 poll 唯一的不同在于：此队列为空时将抛出一个异常。
     * @param o
     * @return
     */
    @Override
    public E remove() {
        return null;
    }

    /**
     * 查看队列头部节点, 如果为空则抛出异常
     * @return
     */
    @Override
    public E element() {
        return null;
    }

    /**
     * 获取但不移除此队列的头；如果此队列为空，则返回 null。
     * @return
     */
    @Override
    public E peek() {
        return null;
    }


    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public int remainingCapacity() {
        return 0;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        return 0;
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        return 0;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }

    //链表队列节点
    static class Node<E> {
        //节点内容
        E item;
        //下一个节点
        Node<E> next;

        Node(E x) { item = x; }
    }
}
