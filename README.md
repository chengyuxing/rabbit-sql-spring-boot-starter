# rabbit-sql-spring-boot-starter

基于 **rabbit-sql** 制作的**spring-boot**自动装配**starter**，

默认使用spring的事务管理，方法头上可通过注解 `@Transactional` 生效或者手动使用spring提供的事务管理器。

:warning: 请勿使用rabbit内置的`Tx`事务，事务已完全由spring全局事务替代。

关于rabbit-sql的使用方法可以具体看[文档](https://github.com/chengyuxing/rabbit-sql/tree/rabbit-sql-7)。

## maven dependency (jdk1.8)

```xml
<dependency>
    <groupId>com.github.chengyuxing</groupId>
    <artifactId>rabbit-sql-spring-boot-starter</artifactId>
    <version>2.1.4</version>
</dependency>
```

## 配置说明

`application.yml` 必要配置：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://127.0.0.1:5432/postgres
    driver-class-name: org.postgresql.Driver
    username: chengyuxing
    password:
```

即可使用**依赖注入**`Baki`进行一些操作：

```java
@Autowired
Baki baki;
```

### 自定义配置

通过IntelliJ IDEA编辑`application.yml`输入**baki**可提示所有配置项，一个简单的例子如下：

`application.yml`

```yaml
baki:
  xql-file-manager:
    files:
      a: xql/one.sql
      b: xql/two.sql
    constants:
      db: test
  debug-full-sql: true
```

配置了`xql-file-manager`属性的情况下，还可以单独注入`XQLFileManager`独立使用，如：

```java
@Autowired
XQLFileManager xqlFileManager;
```

**简单使用**：

```java
@SpringBootApplication
public class Startup implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(Startup.class, args);
    }

    @Autowired
    Baki baki;
    @Autowired
    XQLFileManager xqlFileManager;

    @Override
    public void run(String... args) throws Exception {
        System.out.println(baki.query("select now()").maps());
        try (Stream<DataRow> s = baki.query("&a.region").arg("id", 5).stream()) {
            s.forEach(System.out::println);
        }
    }
}
```

**基于spring管理的事务**：

```java

@Service
public class MyService {

    @Autowired
    Baki baki;

    @Transactional
    public void some() {
        baki.insert("test.tx").save(Args.of("a", 1));
        int i = 1 / 0;    // 抛出异常回滚
        baki.insert("test.tx").save(Args.of("a", 2));
    }
}
```

