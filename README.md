# rabbit-sql-spring-boot-starter

Language: English | [简体中文](README.chs.md)

## Introducing

**It's not instead of any ORM framework**, no conflict with ORM framework, just a lib.

Spring-boot autoconfigure starter based on **rabbit-sql**, use spring managed transaction as default, use `@Transactional` annotation or inject `SimpleTx` (simple wrapper for spring transaction) to use transaction.

- support `application.yml` auto complete;
- compatible with spring jdbc transaction;
- compatible with mybatis, spring-data-jpa and so on to use transaction together;

:warning: don't use rabbit-sql's built-in `Tx`, use spring transaction instead.

get more usage about **rabbit-sql** from [document](https://github.com/chengyuxing/rabbit-sql)。

## Maven dependency (jdk1.8)

```xml
<dependency>
    <groupId>com.github.chengyuxing</groupId>
    <artifactId>rabbit-sql-spring-boot-starter</artifactId>
    <version>2.2.10</version>
</dependency>
```

## IDEA plugin support

Plugin market: [Rabbit sql](https://plugins.jetbrains.com/plugin/21403-rabbit-sql).

## Configuration

`application.yml` (`spring.datasource` is required):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://127.0.0.1:5432/postgres
    driver-class-name: org.postgresql.Driver
    username: chengyuxing
    password:
```

Inject `Baki` ready to use:

```java
@Autowired
Baki baki;
```

### Custom configuration

begin with input **baki** to edit `application.yml`, a simple example look like:

`application.yml`

```yaml
baki:
  xql-file-manager:
    files:
      a: xql/one.sql
      b: xql/two.sql
```

if `xql-file-manager` configured, you can inject `XQLFileManager`  to use without Baki:

```java
@Autowired
XQLFileManager xqlFileManager;
```

**simple usage**:

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

**work with spring transaction**:

```java

@Service
public class MyService {

    @Autowired
    Baki baki;

    @Transactional
    public void some() {
        baki.insert("test.tx").save(Args.of("a", 1));
        int i = 1 / 0;    // will be rollback
        baki.insert("test.tx").save(Args.of("a", 2));
    }
}
```

