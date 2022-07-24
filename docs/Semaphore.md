# 信号量

## 信号量的作用

### 基本原理

信号量（Semaphore）是用于限制访问某资源的线程数量的，有一个许可数量的值N；

线程获取许可成功会减去线程成功获取的许可数量，剩余许可数量小于线程要求的许可数量，线程要么阻塞等待，要么取消要做的工作退出; 

线程成功获取许可，可以继续执行工作，工作完成需要释放许可，信号量的许可数量加上线程释放的许可数量。

> 类比：比较火爆的餐馆，经常因为桌位占满，很多人在门外排队等待。店里的桌位就是N, 店内就餐的人就是获取到许可工作的线程，店外排队的人就是未获取到许可阻塞等待的线程，当然还有不想排队已经离开的。

### 使用场景

+ **限流算法**

  + 令牌桶算法

    案例参考：Guava RateLimiter。

+ **通信组件连接池**

  + Redisson客户端连接数控制

    这里Redisson是自行实现了个信号量。具体参考：AsyncSemaphore。

    > Redisson AsyncSemphore实现原理分析参考：kwseeker/develop-kit 。

+ **线程间通信**

  感觉这种说法是将线程通信和阻塞、等待、通知、唤醒那些混为一谈了，个人感觉这么说很别扭。

  说可以控制线程间通信是不是好点。

+ **其他**

  + 当作同步锁使用

    当许可数量为1, 就是一个同步锁。

    