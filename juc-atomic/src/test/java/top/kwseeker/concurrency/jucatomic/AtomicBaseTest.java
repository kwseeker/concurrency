package top.kwseeker.concurrency.jucatomic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.Assert;
import org.junit.Test;
import sun.misc.Unsafe;

public class AtomicBaseTest {

    @Test
    public void testUnsafeOperation() {
        Unsafe unsafe = UnsafeOperator.getUnsafe();
        long ageOffset = UnsafeOperator.getFieldOffset(User.class, "age");
        User user = new User("Arvin", 18);
        unsafe.compareAndSwapInt(user, ageOffset, 18, 19);  //即使User没有setter方法，成员都是final的也可以成功
        Assert.assertEquals(19, user.getAge());
    }

    @Getter
    @AllArgsConstructor
    static class User {
        private final String name;
        private final int age;
    }
}
