package top.kwseeker.concurrency.jucatomic;

import org.junit.Test;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AtomicReference 实现对“对象引用”操作的原子封装
 *
 * 通过 Unsafe 的 object 相关操作方法实现，依赖 volatile 保证目标引用的可见性
 *
 * 核心代码：
 *      private volatile V value;
 *      private static final Unsafe unsafe = Unsafe.getUnsafe();
 *      private static final long valueOffset;
 *      valueOffset = unsafe.objectFieldOffset(AtomicReference.class.getDeclaredField("value"));
 *      unsafe.putOrderedObject(this, valueOffset, newValue);
 *      unsafe.compareAndSwapObject(this, valueOffset, expect, update)
 *      unsafe.getAndSetObject(this, valueOffset, newValue);
 * 使用场景：
 *      找了一下，找到一个感觉最贴切的DEMO, 是一个车牌竞拍的场景(模拟5人参与一轮竞拍，先拍先得), 来源：https://m.imooc.com/wiki/ctoolslesson-atomicreference
 *      这一轮竞拍中，只有最先竞拍的才能成功；其他稍慢的竞拍都无效；竞拍失败的只能等下一轮竞拍。
 *      这里每次竞拍成功都会新建 CarLicenseTag 对象并更新 carLicenseTag 的引用，通过 CAS 可以确保其中只有一个成功
 */
public class AtomicReferenceTest {

    @Test
    public void testAtomicReference() throws InterruptedException {
        // 代表待拍的车牌
        CarLicenseTag carLicenseTag = new CarLicenseTag(80000);
        // 创建一个 AtomicReference 对象，对车牌对象做原子引用封装
        AtomicReference<CarLicenseTag> carLicenseTagAtomicReference = new AtomicReference<>(carLicenseTag);

        // 定义5个客户进行竞拍
        //CountDownLatch latch = new CountDownLatch(5);
        for (int i = 1; i <= 5; i++) {
            AuctionCustomer carAuctionCustomer = new AuctionCustomer(carLicenseTagAtomicReference, carLicenseTag, i);
            // 开始竞拍
            new Thread(carAuctionCustomer).start();
            //latch.countDown();
        }

        Thread.sleep(100000);
    }

    static class CarLicenseTag {
        // 每张车牌牌号事先是固定的
        private final String licenseTagNo = "沪X66666";
        // 车牌的最新拍卖价格
        private double price = 80000.00;

        public CarLicenseTag(double price) {
            this.price += price;
        }

        public String toString() {
            return "CarLicenseTag{licenseTagNo='" + licenseTagNo + ", price=" + price + '}';
        }
    }

    static  class AuctionCustomer implements Runnable {

        private AtomicReference<CarLicenseTag> carLicenseTagReference;
        private CarLicenseTag carLicenseTag;
        private String customerNo;

        public AuctionCustomer(AtomicReference<CarLicenseTag> carLicenseTagReference, CarLicenseTag carLicenseTag, int customerNo) {
            this.carLicenseTagReference = carLicenseTagReference;
            this.carLicenseTag = carLicenseTag;
            this.customerNo = "第" + customerNo + "位客户";
        }

        public void run() {
            // 客户竞拍行为 (模拟竞拍思考准备时间4秒钟)
            try {
                Thread.sleep(new Random().nextInt(4000));
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 举牌更新最新的竞拍价格
            // 此处做原子引用更新
            boolean bool = carLicenseTagReference.compareAndSet(carLicenseTag,
                    new CarLicenseTag(new Random().nextInt(1000)));
            System.out.println("第" + customerNo + "位客户竞拍" + bool + " 当前的竞拍信息" + carLicenseTagReference.get().toString());
        }
    }
}
