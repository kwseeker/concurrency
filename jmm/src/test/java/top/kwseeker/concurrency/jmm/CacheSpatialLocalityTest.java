package top.kwseeker.concurrency.jmm;

import org.junit.Test;

/**
 * CPU缓存技术 - 空间局部性测试
 * <p>
 * 如果一个存储器的位置被引用，那么将来他附近的位置也会被引用。
 * 比如顺序执行的代码、连续创建的两个对象、数组等。
 * <p>
 * 对二位数组的值进行求和运算，第一次一行行地加，第二次一列列地加。
 */
public class CacheSpatialLocalityTest {

    private static final int RUNS = 100;
    private static final int DIMENSION_1 = 1024 * 1024;
    private static final int DIMENSION_2 = 6;
    private static long[][] longs;

    @Test
    public void testCacheSpatialLocality() {
        //初始化数组，1M*6的二位数组全部填充1L
        longs = new long[DIMENSION_1][];
        for (int i = 0; i < DIMENSION_1; i++) {
            longs[i] = new long[DIMENSION_2];
            for (int j = 0; j < DIMENSION_2; j++) {
                longs[i][j] = 1L;
            }
        }
        System.out.println("Array初始化完毕....");

        //CPU每次取出一行的数据（6个）
        long sum = 0L;
        long start = System.currentTimeMillis();
        for (int r = 0; r < RUNS; r++) {
            for (int i = 0; i < DIMENSION_1; i++) {
                for (int j = 0; j < DIMENSION_2; j++) {
                    sum += longs[i][j];
                }
            }
        }
        System.out.println("第一次求和花费时间:" + (System.currentTimeMillis() - start));
        System.out.println("sum1:" + sum);

        //CPU每次取出１个数据
        sum = 0L;
        start = System.currentTimeMillis();
        for (int r = 0; r < RUNS; r++) {
            for (int j = 0; j < DIMENSION_2; j++) {
                for (int i = 0; i < DIMENSION_1; i++) {
                    sum += longs[i][j];
                }
            }
        }
        System.out.println("第二次求和花费时间:" + (System.currentTimeMillis() - start));
        System.out.println("sum2:" + sum);
    }
}
