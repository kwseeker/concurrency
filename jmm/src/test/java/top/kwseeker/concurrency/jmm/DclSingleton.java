package top.kwseeker.concurrency.jmm;

public class DclSingleton {
    //1 volatile
    private volatile static DclSingleton instance;
    //2 private构造器
    private DclSingleton() {
    }

    public static DclSingleton getInstance() {
        if (instance == null) {
            synchronized (DclSingleton.class) {
                if (instance == null) {
                    //下面一行代码其实分为多个操作:
                    //１创建一个对象，并将其引用值压入栈顶（分配对象内存）
                    //２复制栈顶数值并将复制值压入栈顶
                    //３调用超类构造方法，实例初始化方法，私有方法（对象初始化）
                    //４为指定的类的静态域赋值(将引用赋值给instance)
                    //1->2->3 1->2->4, 3和4没有依赖关系，可能重排序
                    // NEW top/kwseeker/concurrency/jmm/DclSingleton
                    // DUP
                    // INVOKESPECIAL top/kwseeker/concurrency/jmm/DclSingleton.<init> ()V
                    // PUTSTATIC top/kwseeker/concurrency/jmm/DclSingleton.instance : Ltop/kwseeker/concurrency/jmm/DclSingleton;
                    instance = new DclSingleton();      //没有volatile修饰时可能先 putstatic 然后 invokespecial, 这时返回 null
                }
            }
        }
        return instance;
    }
}
