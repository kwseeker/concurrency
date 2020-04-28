package top.kwseeker.concurrency.juccollection.copyonwrite;

import java.util.concurrent.CopyOnWriteArraySet;

public class CopyOnWriteArraySetExample {

    public static void main(String[] args) {
        CopyOnWriteArraySet<String> set = new CopyOnWriteArraySet<>();
        set.add("a");
        set.add("b");
        set.add("a");
    }
}
