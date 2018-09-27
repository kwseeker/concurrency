package top.kwseeker.concurrency.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 这几个自定义的注解都只是为了标注，没有实际功能
 */
//@Target(ElementType.TYPE)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)  //SOURCE类型注解只是标注的用途，编译时会被删除; CLASS编译期保留运行期不保留;  RUNTIME运行期也保留可以通过反射获取
public @interface ThreadSafe {
    String value() default "";
}
