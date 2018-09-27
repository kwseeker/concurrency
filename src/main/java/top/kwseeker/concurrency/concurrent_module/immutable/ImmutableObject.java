package top.kwseeker.concurrency.concurrent_module.immutable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;

/**
 * 不可变对象（对应的类称为不可变类，是线程安全的， 实现以及使用可以参考String， String类是不可变类的典型）
 *
 * 不可变对象的特点：
 *      对象创建以后其状态就不能修改
 *      对象所有域都是final类型
 *      对象创建期间this引用没有逸出（没有连接未初始化完成的this引用和对象）
 *
 * 如何创建一个自己的不可变类：
 *      1）所有成员都是private final
 *      2）不提供对成员的改变方法，例如：setXXXX
 *      3）确保所有的方法不会被重载。手段有两种：使用final Class(强不可变类)，或者将所有类方法加上final(弱不可变类)。
 *      4）如果某一个类成员不是原始变量(primitive)或者不可变类，则必须保证客户端无法获得指向这些对象的引用。
 *      并且，永远不要用客户端提供的对象引用来初始化这样的域，也不要从任何访问方法中返回该对象引用。
 *      在需要接受或返回引用的地方，使用保护性拷贝技术。
 *
 * 定义不可变对象的方法
 *      1）按照上面要求编写
 *      2）使用 Collections.unmodifiableXXX() 指定某个对象
 *      3）使用 Guava ImmutableXXX
 *
 * 不可变对象使用场景
 *      因为不可变对象是线程安全的，多线程使用也无需添加同步措施，本身又是值不可变的；由此可以得出适用场景
 *      1）数据变化不频繁的并发场景下
 */
@Slf4j
public class ImmutableObject {

    // 3 使用Guava 的 ImmutableXXX 实例化对象（下面实例化了4个不可变对象）
    private final static ImmutableList<Integer> list = ImmutableList.of(1, 2, 3);
    private final static ImmutableSet set = ImmutableSet.copyOf(list);
    private final static ImmutableMap<Integer, Integer> map = ImmutableMap.of(1, 2, 3, 4);
    private final static ImmutableMap<Integer, Integer> map2 = ImmutableMap.<Integer, Integer>builder()
            .put(1, 2).put(3, 4).put(5, 6).build();

    public static void main(String[] args) {
        System.out.println(map2.get(3));
    }

    //========================================================================================
    // 2 使用 Collections.unmodifiableXXX() 定义不可变对象（内容不能更改）
//    private static Map<Integer, Integer> map = Maps.newHashMap();
//
//    static {
//        map.put(1, 2);
//        map.put(3, 4);
//        map.put(5, 6);
//        map = Collections.unmodifiableMap(map);   // map引用的对象变为不可变的对象
//    }
//
//    public static void main(String[] args) {
//        map.put(1, 3);
//        log.info("{}", map.get(1));
//    }

    //========================================================================================
    // 1 遵循不可变类的原则实现不可变类
//    private final Integer a;
//    private final String b;
//    //这里虽然引用了一个可变的类，但是仍被称作不可变类，但是为了保证类的不可改变，
//    //必须通过在成员初始化(in)或者get方法(out)时通过深度clone方法。即从原始的成员对象那里获取值
//    private final Map<Integer, Integer> map;
//
//    public ImmutableObject(Integer a, String b, Map map) {
//        this.a = a;
//        this.b = b;
//        this.map = map;
//    }
//
//    public final Map<Integer, Integer> getMap() {
//        //因为 Map 不是不可变类，需要复制 map 的值到一个新的Map对象，然后将新的map对象返回
//        return  Collections.unmodifiableMap(map);   //使得外界无法获得实例对象原本的对象，这里是创建了一个新的map实例返回，外界就无法改变原来对象的map内的值了
//    }
//    public final Integer getA() {
//        //Integer本身为不可变类，直接返回即可
//        return a;
//    }
//    public final String getB() {
//        return b;
//    }
//
//    @Override
//    public final String toString() {
//        return "a:" + getA() + ", b:" + getB() + ", map:" + getMap().toString();
//    }
//
//    public static void main(String[] args) {
//        Map<Integer, Integer> map = Maps.newHashMap();
//        map.put(3, 3);
//        map.put(4, 4);
//
//        ImmutableObject immutableObject = new ImmutableObject(1, "2", map); //从这里开始 immutableObject指向对象不可改变
//        log.info("初始化之后: {}", immutableObject.toString());
//
//        //后面不管怎样，都无法改变 immutableObject 内部的值
//    }
}
