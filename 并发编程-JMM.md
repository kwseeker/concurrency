[TOC]

## 1 Java内存模型概述

内存模型描述了线程间通过内存和共享数据的交互。

Java内存模型是对各种机器内存模型的一个映射抽象。

为什么需要定义内存模型？因为CPU缓存和指令重排的操作复杂性，需要用内存模型定义操作流程，决定线程什么时候可以读取其他线程写的数据，保证数据一致性。

+ **CPU缓存与缓存一致性**

  主流CPU缓存架构：

  一级缓存（每个CPU核心独有） -> 二级缓存（每个CPU核心独有） -> 三级缓存（所有核心共享） -> 主内存

  一二级缓存又被称为线程本地缓存。

  多线程多核心处理，一个共享的对象在一二级缓存上会有多个副本，由此带来缓存一致性的问题。 

  保证CPU缓存一致性：

  + Lock总线

    加锁机制，共享对象只能在一个CPU核心上被使用，不允许同时出现在多个CPU核心的缓存中，性能低下。

  + <u>缓存一致性协议MESI</u>

    现在多用MESI保证缓存一致性，共享对象允许在多个CPU核心的缓存上存在，拥有四种状态：Shared、Exclusive、Modified、Invalid。

    ![MESI状态迁移](http://imgedu.lagou.com/d98244d3850e4721b27cfdbee89c34ae.jpg)

    ![](https://img-blog.csdn.net/20180531185023380?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMzNDMyNTU5/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

+ **指令重排**

  为了提升性能，代码执行从编译到执行，指令可能会先后被编译器、CPU、内存系统重排序。

  源代码 -> 编译器优化重排序 -> 指令级并行重排序 -> 内存系统重排序 -> 最终执行的指令序列

+ **缓存一致性模型**

  + 顺序一致性语义

    Lamport提出的操作规范：

    R1: 一个线程内的所有操作必须按照程序的顺序来执行。

    R2: 所有线程都只能看到相同的操作执行视图（是否同步）。

    这种规范不允许指令重排优化，执行效率不高。

  - <u>happen-before（JMM采用的缓存一致性模型）</u>

    happen-before定义：一个操作A happen-before另一个操作B，指A操作在B操作之前执行;

    <u>两个操作之间的happen-before关系是通过锁和volatile、final定义的</u>。

    <u>操作A happen-before 操作B，则在MESI的控制下，A操作结果对B是可见的（A将结果写回主存并通知B所在线程所持有的副本失效需要从主存重新取）</u>。

    操作规范：

    1）对锁的操作（monitor-synchronized / Lock）：对一个锁的解锁，一定happen-before后面对这个锁的加锁。

    2）对volatile变量的读写：对一个volatile变量的写，一定happen-before后续对它的读。

    ​	基于load-store指令集。

    3）happen-before 具有传递性。

    

## 2 Java内存划分与使用

对象的基本类型成员、类成员、方法、方法内的变量都是存储在哪个内存空间？多线程会共享哪些内存空间？



## 3 多线程哪些内容是共享的

为什么对象的方法内的变量不是多线程共享的？



## 4 测试案例

#### 4.1 下面一段程序可能的执行结果

```java
int x=0, y=0;
Thread1: y=1;println(x);
Thread2: x=1;println(y);
```



## 附录 

在学习 JVM 后再反复看看内存模型。

参考资料：

《计算机体系结构：量化研究方法》亨尼西

[Java Memory Model Under The Hood](https://gvsmirnov.ru/blog/tech/2014/02/10/jmm-under-the-hood.html)

