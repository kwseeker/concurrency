package top.kwseeker.unsafe;

import junit.framework.TestCase;
import org.junit.Test;

public class AtomicIntegerArrayTest extends TestCase {

    @Test
    public void testInteger() {
        int i = 4;
        System.out.println(Integer.numberOfLeadingZeros(i));
    }
}