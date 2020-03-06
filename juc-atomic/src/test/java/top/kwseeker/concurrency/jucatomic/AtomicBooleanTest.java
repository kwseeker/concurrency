package top.kwseeker.concurrency.jucatomic;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * lazySet？
 * weakCompareAndSet？
 */
public class AtomicBooleanTest {

    @Test
    public void testAtomicBoolean() {
        AtomicBoolean value = new AtomicBoolean();
        boolean ret = value.compareAndSet(false, true);
        boolean result = value.get();
    }
}
