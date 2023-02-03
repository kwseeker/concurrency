package top.kwseeker.concurrency.threadlocal;

import java.lang.ref.WeakReference;

public class Entry extends WeakReference<Object> {

    public Entry(Object referent) {
        super(referent);
    }

    public static void main(String[] args) throws InterruptedException {
        Entry wrs = new Entry(new Object());
        System.out.println(wrs.get());
        System.gc();
        Thread.sleep(100);
        System.out.println(wrs.get());  //referent == null
    }
}
