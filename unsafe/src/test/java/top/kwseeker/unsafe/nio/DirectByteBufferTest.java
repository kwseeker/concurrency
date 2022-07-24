package top.kwseeker.unsafe.nio;

import junit.framework.TestCase;
import org.junit.Test;
import sun.misc.VM;

public class DirectByteBufferTest extends TestCase {

    @Test
    public void testDirectByteBuffer() {
        boolean pa = VM.isDirectMemoryPageAligned();
        //int ps = Bits.pageSize();
    }
}