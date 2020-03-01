package top.kwseeker.concurrency;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.*;

import static org.openjdk.jcstress.annotations.Expect.*;

public class JMMCase1 {

    //指定使用并发测试
    @JCStressTest
    //预测的结果
    @Outcome(id = {"0, 2", "1, 0", "1, 2"}, expect = ACCEPTABLE, desc = "Trivial under sequential consistency")
    @Outcome(id = "0, 0", expect = FORBIDDEN, desc = "Violates sequential consistency")
    //需要测试的类
    @State
    public static class PlainExecutionOrder {
        //int x, y;
        //volatile  int x; int y;
        volatile int x, y;

        @Actor
        public void actor1(II_Result r) {
            x = 1;
            r.r2 = y;
        }

        @Actor
        public void actor2(II_Result r) {
            y = 2;
            r.r1 = x;
        }
    }

    @JCStressTest
    //预测的结果
    @Outcome(id = {"0, 2", "1, 0", "0, 0"}, expect = ACCEPTABLE, desc = "Trivial under sequential consistency")
    @Outcome(id = "1, 2", expect = FORBIDDEN, desc = "Violates sequential consistency")
    //需要测试的类
    @State
    public static class PlainExecutionOrder3 {
        //int x, y;
        volatile  int x; int y;
        //volatile int x, y;

        @Actor
        public void actor1(II_Result r) {   //禁止重排序， 只要换下位置如上一个测试，就会发生重排序
            r.r1 = x;                       //volatile读
            y = 2;                          //普通写
        }

        @Actor
        public void actor2(II_Result r) {   //禁止重排序
            r.r2 = y;                       //普通读
            x = 1;                          //volatile写
        }
    }

    @JCStressTest
    //预测的结果
    //@Outcome(id = {"true"}, expect = ACCEPTABLE, desc = "Trivial under sequential consistency")
    //@Outcome(id = "false", expect = FORBIDDEN, desc = "Violates sequential consistency")
    //@Outcome(id = {"true"}, expect = ACCEPTABLE, desc = "Trivial under sequential consistency")
    @Outcome(id = "true, 0", expect = FORBIDDEN, desc = "Violates sequential consistency")
    //需要测试的类
    @State
    public static class PlainExecutionOrder4 {

        volatile boolean flag;
        int a;

        @Actor
        public void actor1() {             //不可重排序
            a = 1;                         //普通写
            flag = true;                   //volatile写
        }

        @Actor
        public void actor2(ZI_Result r) {
            //这种测试没有出现 (true,0) 的结果，符合博客上的描述，不会重排序
            r.r1 = flag;                    //volatile读
            r.r2 = a;                       //普通读

            //TODO：这种测试会低概率出现 (true,0) 的结果 ？？？， 和上面什么区别？
            //if(flag) {                      //volatile读
            //    a = a * 1;                  //普通读、普通写
            //}
            //if(a == 0) {                    //普通读
            //    r.r1 = flag;                //volatile读
            //}
        }
    }

    @JCStressTest
    //预测的结果
    @Outcome(id = {"0,2,0", "0,2,3", "1,0,0", "1,0,3", "1,2,0", "1,2,3"}, expect = ACCEPTABLE,
            desc = "Trivial under sequential consistency")
    @Outcome(id = {"0,0,3", "0,0,0"}, expect = FORBIDDEN, desc = "Violates sequential consistency")
    //需要测试的类
    @State
    public static class PlainExecutionOrder2 {
        //int x, y;
        volatile  int x,y; int z;
        //volatile int x, y;

        @Actor
        public void actor1(III_Result r) {
            x = 1;
            r.r2 = y;
            z = 3;
        }

        @Actor
        public void actor2(III_Result r) {
            y = 2;
            r.r1 = x;
            r.r3 = z;
        }
    }

}
