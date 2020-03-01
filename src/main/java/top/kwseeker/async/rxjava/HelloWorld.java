package top.kwseeker.async.rxjava;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

public class HelloWorld {

    public static void main(String[] args) {
        if(args == null || args.length == 0) {
            args = new String[]{"Arvin", "Nancy"};
        }
        Flowable.fromArray(args).subscribe(s -> System.out.println("Hello " + s + "!"));

        //Flowable.fromArray(args).subscribe(new Consumer<String>() {
        //    @Override
        //    public void accept(String s) {
        //        System.out.println("Hello " + s + "!");
        //    }
        //});
    }
}
