# rabbit-sql-spring-boot-starter

[![License][badge:license]][license]
[![Maven][badge:maven]][maven-repository]
[![Version][badge:version]][versions]

Language: English | [简体中文](README.chs.md)

## Introducing

**It's not instead of any ORM framework**, no conflict with ORM framework, just a lib.

Spring-boot autoconfigure starter based on **rabbit-sql**, use spring managed transaction as default, use `@Transactional` annotation or inject `com.github.chengyuxing.sql.spring.autoconfigure.Tx` (simple wrapper for spring transaction) to use transaction.

- support `application.yml` auto complete;
- compatible with spring jdbc transaction;
- compatible with mybatis, spring-data-jpa and so on to use transaction together;

:warning: don't use **rabbit-sql's** built-in `Tx`, use spring transaction instead.

- ~~com.github.chengyuxing.sql.transaction.Tx~~ ❌
- com.github.chengyuxing.sql.spring.autoconfigure.Tx ✅

get more usage about **rabbit-sql** from [document](https://github.com/chengyuxing/rabbit-sql)。

## Maven dependency (jdk1.8)

Maven central

```xml
<dependency>
    <groupId>com.github.chengyuxing</groupId>
    <artifactId>rabbit-sql-spring-boot-starter</artifactId>
    <version>2.10.9</version>
</dependency>
```

## IDEA plugin support

Plugin marketplace: [Rabbit sql](https://plugins.jetbrains.com/plugin/21403-rabbit-sql) and [documentation](https://github.com/chengyuxing/rabbit-sql-plugin#readme).

## Configuration

`application.yml` (`spring.datasource` is required):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://127.0.0.1:5432/postgres
    username: chengyuxing
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
      a: mydir/one.sql
      b: mydir/two.sql
```

### Working with [Rabbit sql plugin](https://plugins.jetbrains.com/plugin/21403-rabbit-sql) 

1. Remove the `xql-file-manager` property from `application.yml` ;
2. Sql file extension must rename to `xql` ;
3. Create `xql-file-manager.yml` in resource root `.../src/main/resources` ;
4. Configure [properties](https://github.com/chengyuxing/rabbit-sql#constructor).

### simple usage

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

[badge:maven]:https://img.shields.io/maven-central/v/com.github.chengyuxing/rabbit-sql-spring-boot-starter
[badge:license]: https://img.shields.io/github/license/chengyuxing/rabbit-sql-spring-boot-starter
[badge:version]:https://img.shields.io/jetbrains/plugin/v/21403

[license]:https://github.com/chengyuxing/rabbit-sql-spring-boot-starter/blob/main/LICENSE
[maven-repository]:https://central.sonatype.com/artifact/com.github.chengyuxing/rabbit-sql-spring-boot-starter
[versions]:https://plugins.jetbrains.com/plugin/21403-rabbit-sql/versions