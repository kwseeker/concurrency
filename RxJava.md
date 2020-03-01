# RxJava 实现异步和基于事件的程序

[RxJava Wiki 官方文档](https://github.com/ReactiveX/RxJava/wiki)

Rx编程模型的Java实现。

[ReactiveX 编程模型](http://reactivex.io/tutorials.html)

[ReactiveX 中文翻译](https://mcxiaoke.gitbooks.io/rxdocs/content/)

简写为Rx，是一个编程模型，目标是提供一致的编程接口，帮助开发者更方便的处理异步数据流；
现在几乎已经支持了所有的编程语言。

Rx让开发者可以利用可观察序列和LINQ风格查询操作符来编写异步和基于事件的程序，
使用Rx，开发者可以用Observables表示异步数据流，用LINQ操作符查询异步数据流， 用Schedulers参数化异步数据流的并发处理，
Rx可以这样定义：Rx = Observables + LINQ + Schedulers。
    
+ Observable

    可观察的对象，可发射数据（事件）  

+ LINQ (语言集成查询)

    LINQ是集成到C#和Visual Basic.NET这些语言中用于提供查询数据能力的一个新特性。

    ```C#
    class Program {
        static void Main(string[] args) {
            int[] numbers = { 2, 12, 5, 15 };   //创建一个int数组对象作为数据源。等价于：int[] numbers = new int[] { 2, 12, 5, 15 };
            IEnumerable<int> lowNums = from n in numbers //定义并存储查询。
                                       where n < 10
                                       select n;
            foreach (var x in lowNums) {    //使用foreach语句遍历集合
                Console.Write("{0},",x);
            }
            Console.ReadKey();
        }
    }
    ```
+ Scheduler

[Reactor 响应式编程库](https://projectreactor.io/docs/core/release/reference/)
[Reactor3 zh](http://htmlpreview.github.io/?https://github.com/get-set/reactor-core/blob/master-zh/src/docs/index.html)

Reactor是另一个响应式编程库

[Spring  Webflux 响应式web框架]()

Spring Webflux 使用Project Reactor实现响应式编程。

> 异步结果的获取：1. 主动轮询（Proactive） 2.被动接收（Reactive）
>
> 响应式编程是异步获取结果的一种方式！ 
>
> Java9 提供了响应式编程接口，且和Reactor的编程规范一致。
>
> 响应式编程的背压（BackPressure）：上层数据生成速度大于下层处理速度，
产生数据堆积情况下，防止数据丢失的一种能力。

## 响应式编程产生原因及应用场景

响应式编程是高负载、多用户应用的优雅的解决方案，像社交应用、游戏、音视频应用；
此外，应用有以下模块也适合应用：需要大量交互的服务端代码、
代理服务器/负载均衡器、人工智能/机器学习、实时数据流处理。

## 响应式编程模型结构

#### Rx编程模型

观察者(Observer/Subscriber/Watcher/Reactor)订阅(Subscribe)一个可观察对象(Observable)。
通过调用观察者的方法，Observable发射数据或者通知给它的观察者。

1. 定义一个方法，这个方法拿着某个异步调用的返回值做一些有用的事情。这个方法是观察者的一部分  
2. 将这个异步调用本身定义为一个Observable  
    Observable分为"冷"、"热"、"Connectable"。  
3. 观察者通过订阅(Subscribe)操作关联到那个Observable  
    观察者的异步回调注册到可观察对象。  
    `onNext(T item)` Observable调用这个方法发射数据；  
    `onError(Exception ex)`   
    `onComplete`  
4. 继续你的业务逻辑，等方法返回时，Observable会发射结果，观察者的方法会开始处理结果或结果集  
    操作符变换、组合、操纵和处理Observable发射的数据。  
    ```
    创建操作 Create, Defer, Empty/Never/Throw, From, Interval, Just, Range, Repeat, Start, Timer
    变换操作 Buffer, FlatMap, GroupBy, Map, Scan和Window
    过滤操作 Debounce, Distinct, ElementAt, Filter, First, IgnoreElements, Last, Sample, Skip, SkipLast, Take, TakeLast
    组合操作 And/Then/When, CombineLatest, Join, Merge, StartWith, Switch, Zip
    错误处理 Catch和Retry
    辅助操作 Delay, Do, Materialize/Dematerialize, ObserveOn, Serialize, Subscribe, SubscribeOn, TimeInterval, Timeout, Timestamp, Using
    条件和布尔操作 All, Amb, Contains, DefaultIfEmpty, SequenceEqual, SkipUntil, SkipWhile, TakeUntil, TakeWhile
    算术和集合操作 Average, Concat, Count, Max, Min, Reduce, Sum
    转换操作 To
    连接操作 Connect, Publish, RefCount, Replay
    反压操作，用于增加特殊的流程控制策略的操作符
    ```

#### Reactor编程模型
    
> 依赖链：一个事件的生成依赖于其他事件触发，由此串起来的事件链
>
> 事件触发，被响应方接收并处理的模式又叫 Pub/Sub 模式, OOP中则称为 Observer 模式

## RxJava 使用

1. 创建 Observable 

2. 使用操作符发送Observable

3. 异常处理

4. 取消订阅

## RxJava 的实现

