package top.kwseeker.concurrency;

/**
 * volatile内存语义的实现
 */
public class VolatileMemoryBarrier {

    int a = 0;
    int b = 0;
    public volatile int m = 1;
    public volatile int n = 2;

    /**
     * 第一个操作是普通读，第二个操作是volatile写，加 LoadStore 内存屏障
     */
    public void case1() {
        int x = a;
        //LoadStore, 结果：这两行代码不会重排，且对下一条语句看来x的值一定是0
        int y = m;
    }

    public void case2() {
        a = 10;
        //StoreStore
        m = 11;
    }

}
