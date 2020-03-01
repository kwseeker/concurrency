## Java并发测试

#### 测试执行命令

```shell
java -XX:+UnlockDiagnosticVMOptions -XX:+WhiteBoxAPI -XX:-RestrictContended -jar target/jcstress-jmm.jar -v -t JMM_CASE{N}.PlainExecutionOrder
```

#### 案例

+ **case1**

  伪代码：

  ```java
  int x, y;
  volatile int x, int y;
  volatile int x, y;
  Thread1:
  	x=1;
  	println(y);
  Thread2：
  	y=1;
  	println(x);
  ```

  代码执行可能打印哪些结果？

  //int x, y;

  //volatile int x, int y;

  (0,0) (0,1) (1,0) (1,1)

  // volatile int x, y;

  (0,1) (1,0) (1,1)

+ **case2**