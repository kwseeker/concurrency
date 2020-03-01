[TOC]

[jcstress](https://wiki.openjdk.java.net/display/CodeTools/jcstress)

[jcstress-sample](http://hg.openjdk.java.net/code-tools/jcstress/file/tip/jcstress-samples/src/main/java/org/openjdk/jcstress/samples)

jcstress（Java Concurrency stress tests）Java并发压力测试是一种实验工具，是一组测试，可帮助研究JVM，类库和硬件中并发的正确性。

简直是一个神器。

TODO：抽空研究下使用方法和实现。

## 1 使用方法

#### 1.1. Maven配置

```xml
    <properties>
        <jcstress.version>0.5</jcstress.version>
        <javac.target>8</javac.target>
        <targetJar.name>jcstress-jmm</targetJar.name>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.openjdk.jcstress</groupId>
            <artifactId>jcstress-core</artifactId>
            <version>${jcstress.version}</version>
        </dependency>
        <dependency>
            <groupId>org.openjdk.jcstress</groupId>
            <artifactId>jcstress-samples</artifactId>
            <version>${jcstress.version}</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- 设置java编译环境 1.8 -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.1</version>
                <configuration>
                    <compilerVersion>${javac.target}</compilerVersion>
                    <source>${javac.target}</source>
                    <target>${javac.target}</target>
                </configuration>
            </plugin>
            <!-- 将测试报告文件纳入mvn clean范围 -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-clean-plugin</artifactId>
                <version>3.1.0</version>
                <configuration>
                    <filesets>
                        <fileset>
                            <directory>${basedir}/results</directory>
                            <followSymlinks>false</followSymlinks>
                        </fileset>
                        <fileset>
                            <directory>${basedir}</directory>
                            <includes>
                                <include>jcstress-results*</include>
                            </includes>
                            <followSymlinks>false</followSymlinks>
                        </fileset>
                    </filesets>
                </configuration>
            </plugin>
            <!-- 将项目打包成可执行jar包 -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>2.2</version>
                <executions>
                    <execution>
                        <id>main</id>
                        <!-- 打包命令 -->
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <!-- 目标jar包名前缀 -->
                            <finalName>${targetJar.name}</finalName>
                            <transformers>
                                <!-- 指定主方法入口 -->
                                <transformer
                                        implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                    <mainClass>org.openjdk.jcstress.Main</mainClass>
                                </transformer>
                                <transformer
                                        implementation="org.apache.maven.plugins.shade.resource.AppendingTransformer">
                                    <resource>META-INF/TestList</resource>
                                </transformer>
                            </transformers>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

上面pom.xml中将项目打包成可执行jar包，是为了在命令行中启动测试，如果在IDEA启动器启动测试，只需要配置启动器不需要配置maven-shade-plugin。

#### 1.2. 测试用例编写



#### 1.3. 启动参数配置

+ 启动参数

  - 查看帮助

    ```shell
    # 查看程序参数说明
    java -jar target/xxx.jar -h
    ```

  + JVM参数

    查看所有-XX参数
    
    ```shell
    $ java -client -XX:+PrintFlagsFinal
    ```

    

+ 命令行启动测试

  ```shell
  java -XX:+UnlockDiagnosticVMOptions -XX:+WhiteBoxAPI -XX:-RestrictContended -jar target/xxx.jar -v -t xxx.xxx
  ```

  

+ IDEA启动器启动测试

  

## 2 实现原理

