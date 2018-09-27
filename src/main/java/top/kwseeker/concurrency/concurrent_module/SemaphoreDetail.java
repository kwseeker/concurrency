package top.kwseeker.concurrency.concurrent_module;

import java.util.concurrent.Semaphore;

/**
 * Semaphore 用途
 *  1）保护一段代码最多有N个线程进入；
 *  2）线程间通信。
 *
 * Semaphore 信号量 (java.util.concurrent.Semaphore) 是一个计数信号量
 * 计数信号量由一个指定数量N的"许可"初始化。
 * 每调用一次acquire(), 一个许可会被调用线程取走；
 * 每调用一次release(), 一个许可会被返还给信号量。
 * 在没有许可使用时，无法执行新的线程，从而保护一个程序最多只有N个线程进入。
 *
 * Semaphore Semaphore(int permits) 获取许可的公平性不能保证
 *           Semaphore(int permits, boolean fair) 当fair为true 可以保证公平性
 */
public class SemaphoreDetail {

//    class Pool {
//        private static final int MAX_AVAILABLE = 100;
//        private final Semaphore available = new Semaphore(MAX_AVAILABLE, true);
//
//        public Object getItem() throws InterruptedException {
//            available.acquire();
//            return getNextAvailableItem();
//        }
//
//        public void putItem(Object x) {
//            if (markAsUnused(x))
//                available.release();
//        }
//
//        // Not a particularly efficient data structure; just for demo
//
//        protected Object[] items = new Object[];
//        protected boolean[] used = new boolean[MAX_AVAILABLE];
//
//        protected synchronized Object getNextAvailableItem() {
//            for (int i = 0; i < MAX_AVAILABLE; ++i) {
//                if (!used[i]) {
//                    used[i] = true;
//                    return items[i];
//                }
//            }
//            return null; // not reached
//        }
//
//        protected synchronized boolean markAsUnused(Object item) {
//            for (int i = 0; i < MAX_AVAILABLE; ++i) {
//                if (item == items[i]) {
//                    if (used[i]) {
//                        used[i] = false;
//                        return true;
//                    } else
//                        return false;
//                }
//            }
//            return false;
//        }
//    }

}
