# rabbit-sql-spring-boot-starter

语言: [English](README.md) | 简体中文

## 前言

**没有任何一个框架能独立漂亮解决任何问题**，配合才是最好的解决方案，这不是用来替换ORM等任何jdbc框架，而是作为辅助，与ORM框架相互配合，复杂sql交给rabbit-sql来进行管理。

### 再次声明

**这不是用来替换ORM等任何jdbc框架**，和ORM框架完全不冲突，这只是一个工具，一个库！！！

## 介绍

基于 **rabbit-sql** 制作的**spring-boot**自动装配**starter**，默认使用spring的事务管理，方法头上可通过注解 `@Transactional` 生效或者手动注入 `SimpleTx` （对spring事务的简易封装）来使用事务。

- 支持application.yml配置项自动完成提示；
- 兼容spring jdbc事务；
- 兼容mybatis、spring-data-jpa等同时进行事务处理；

:warning: 请勿使用rabbit内置的`Tx`事务，事务已完全由spring全局事务替代。

关于rabbit-sql的使用方法可以具体看[文档](https://github.com/chengyuxing/rabbit-sql)。

## maven dependency (jdk1.8)

```xml
<dependency>
    <groupId>com.github.chengyuxing</groupId>
    <artifactId>rabbit-sql-spring-boot-starter</artifactId>
    <version>2.2.11</version>
</dependency>
```

## IDEA 插件支持

插件商店搜索：[Rabbit sql](https://plugins.jetbrains.com/plugin/21403-rabbit-sql)。

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

    @Override
    public void run(String... args) throws Exception {
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

