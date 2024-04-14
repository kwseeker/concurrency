# sleep() 和 wait()

线程 sleep() 和 Object wait() 特点测试：

1. 对于 sleep()方法，我们首先要知道该方法是属于 Thread 类中的。而 wait() 方法，则是属于 Object 类中的。
2. sleep()方法导致了程序暂停执行指定的时间，让出 cpu 给其他线程，但是他的监控状态依然保持着，当指定的时间到了又会自动恢复运行状态。
3. wait() notify() 依赖 synchronized 监视器锁才能正常工作。
4. 在调用 sleep()方法的过程中，线程不会释放监视器锁。
5. 当调用 wait()方法的时候，线程会放弃监视器锁，进入等待此对象的等待锁定池，只有针对此对象调用 notify()方法后本线程才进入对象锁定池准备获取对象锁进入运行状态。

测试 Demo: ThreadSleepTest.java。
