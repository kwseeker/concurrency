package top.kwseeker.concurrency.concurrent_module.lock;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ReentrantReadWriteLock 是悲观锁
 *      读写锁的出现是因为人们发现读与读之间没有互斥问题，读与写、写与写才有互斥问题。
 *      使用读写锁相对于其他的锁可以提高读的性能。
 *
 * 悲观锁和乐观锁是面对读写互斥措施的概念，对应悲观读（读写互斥，一定要相互加锁）和乐观读（没关系，反正读相对于写很少，出了问题再补救就行了）
 *      悲观读：读的时候必加写锁
 *      乐观读：读操作很多写操作很少情况下，可以乐观地认为写入与读取同时发生的几率很小，因此不（悲观地）使用完全
 *              的读取锁定（即写的时候并不加读锁），在读取完成后查看是否有写入变更，然后再才去补救措施。
 *
 *      在读多写少情况下，乐观读机制比悲观读在写的性能上有了很大的提升。 乐观锁参考StampedLock的实现。
 */
//读时加读锁防止同时有写操作，写时加写锁，防止同时有读或写
public class ReentrantReadWriteLockDemo {

    private final Map<String, Data> map = new TreeMap<>();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    public Data get(String key) {
        try {
            readLock.lock();
            return map.get(key);
        } finally {
            readLock.unlock();
        }
    }

    public Set<String> getAllKeys() {
        try {
            readLock.lock();
            return map.keySet();
        } finally {
            readLock.unlock();
        }
    }

    public Data put(String key, Data value) {
        try {
            writeLock.lock();
            return map.put(key, value);
        } finally {
            readLock.unlock();
        }
    }

    class Data {
    }
}
