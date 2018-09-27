package top.kwseeker.concurrency.concurrent_module.publish_class;

import lombok.extern.slf4j.Slf4j;

/**
 * 对象逸出
 * 当一个对象还没有构造完成时，就使它被其他线程所见。
 */
@Slf4j
public class Escape {

    private int thisCanBeEscape = 0;

    public Escape() {
        new InnerClass();   //主类还没有构造完成，内部类就开始访问主类的私有成员了
    }

    private class InnerClass {
        public InnerClass() {
            log.info("{}", Escape.this.thisCanBeEscape);
        }
    }

    public static void main(String[] args) {
        new Escape();
    }
}
