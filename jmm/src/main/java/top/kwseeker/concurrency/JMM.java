package top.kwseeker.concurrency;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.II_Result;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE_INTERESTING;

public class JMM {

    @JCStressTest
    @Outcome(id = {"0, 1", "1, 0", "1, 1"}, expect = ACCEPTABLE, desc = "Trivial under sequential consistency")
    @Outcome(id = "0, 0", expect = ACCEPTABLE_INTERESTING, desc = "Violates sequential consistency")
    @State
    public static class PlainExecutionOrder {
        int x, y;
        int i, j;

        @Actor
        public void actor1(II_Result r) {
            x = 1;
            r.r2 = y;
        }

        @Actor
        public void actor2(II_Result r) {
            y = 1;
            r.r1 = x;
        }
    }
}
