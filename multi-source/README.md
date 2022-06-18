@[TOC](Spring Boot 动态数据源)
> 利用AOP来实现多数据源的动态切换功能。
# 1.前言
> AbstractRoutingDataSource是Spring2.0.1版本引入的一个抽象类，它提供了多数据源的支持能力。AbstractRoutingDataSource抽象类定义了抽象的determineCurrentLookupKey方法，子类只需实现此方法，进而动态确定要使用的数据源。

AbstractRoutingDataSource
```java
public abstract class AbstractRoutingDataSource extends AbstractDataSource implements InitializingBean {
    @Nullable
    private Map<Object, Object> targetDataSources;
    @Nullable
    private Object defaultTargetDataSource;
    private boolean lenientFallback = true;
    private DataSourceLookup dataSourceLookup = new JndiDataSourceLookup();
    @Nullable
    private Map<Object, DataSource> resolvedDataSources;
    @Nullable
    private DataSource resolvedDefaultDataSource;
    
    ......

    protected DataSource determineTargetDataSource() {
        Assert.notNull(this.resolvedDataSources, "DataSource router not initialized");
        Object lookupKey = this.determineCurrentLookupKey();
        DataSource dataSource = (DataSource)this.resolvedDataSources.get(lookupKey);
        if (dataSource == null && (this.lenientFallback || lookupKey == null)) {
            dataSource = this.resolvedDefaultDataSource;
        }

        if (dataSource == null) {
            throw new IllegalStateException("Cannot determine target DataSource for lookup key [" + lookupKey + "]");
        } else {
            return dataSource;
        }
    }

    @Nullable
    protected abstract Object determineCurrentLookupKey();
}
```

1. 调用determineCurrentLookupKey方法来获取数据源名称key
2. 从resolvedDataSources属性中得到对应的DataSource对象。
3. 如果找不到DataSource对象或者数据源名称key不存在则使用resolvedDefaultDataSource。

# 2.实现思路

1. 提前准备好多个数据源
2. 将其存入一个Map中 （Map的Key是对应数据源的名称，而Value则是对应的数据源）
3. 将Map设置到AbstractRoutingDataSource对象的resolvedDataSources属性中
4. 当执行数据库操作的时候就通过一个Key来从Map中获取对应的数据源实例
5. 执行对应的数据库操作

# 3.项目实战
## 3.1 项目初始化
新建一个spring的项目：multi-source
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.0</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>cn.flowboot</groupId>
    <artifactId>multi-source</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>multi-source</name>
    <description>multi-source</description>
    <properties>
        <java.version>1.8</java.version>
        <!-- mybatis plus 依赖 -->
        <mybatis-plus.version>3.5.2</mybatis-plus.version>
        <!-- druid 依赖 -->
        <druid.version>1.2.6</druid.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-starter</artifactId>
            <version>${druid.version}</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>

```
> 这里使用了mybatis plus、druid连接池

## 3.2 配置
application.yml 配置环境
```yaml
spring:
  profiles:
    active: multiple
```
application-multiple.yml 配置数据源

```yaml
# 数据源配置
spring:
  datasource:
    # 数据源类型
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    # 自定义数据源
    ds:
      # 主数据源，默认为master
      master:
        url: jdbc:mysql://127.0.0.1:3306/ds1?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=Asia/Shanghai
        username: root
        password: root
      # 从数据源，slave
      slave:
        url: jdbc:mysql://127.0.0.1:3306/ds2?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=Asia/Shanghai
        username: root
        password: root
    # 初始连接数
    initial-size: 5
    # 最小连接池数量
    min-idle: 10
    # 最大连接池数量
    max-active: 20
    # 获取连接等待超时的时间
    max-wait: 60000
    # 检测间隔时间，检测需要关闭的空闲连接，单位毫秒
    time-between-eviction-runs-millis: 60000
    # 一个连接在连接池中最小的生存时间，单位毫秒
    min-evictable-idle-time-millis: 300000
    # 一个连接在连接池中最大的生存时间，单位毫秒
    max-evictable-idle-time-millis: 900000
    # 配置检测连接是否有效
    validation-query: SELECT 1 FROM DUAL
    # 如果为true（默认为false），当应用向连接池申请连接时，连接池会判断这条连接是否是可用的
    test-on-borrow: false
    # 连接返回检测
    test-on-return: false
    # 失效连接检测
    test-while-idle: true
    druid:
      web-stat-filter:
        enabled: true
      stat-view-servlet:
        enabled: true
        # 设置白名单，缺省为所有
        allow:
        url-pattern: /druid/*
        # 登录用户名及密码
        login-username: melody
        login-password: melody
      filter:
        # 开启统计功能
        stat:
          enabled: true
          # 开启慢查询功能
          log-slow-sql: true
          slow-sql-millis: 1000
          # 合并多SQL
          merge-sql: true
        # 开启防火墙功能
        wall:
          enabled: true
          config:
            # 允许多语句同时执行
            multi-statement-allow: true
```


## 3.3 配置类
MultipleDSConfiguration 多数据源配置
```java
import com.alibaba.druid.pool.DruidDataSource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * <h1>多数据源配置</h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */
@Component
@ConfigurationProperties(prefix = "spring.datasource")
@Data
public class MultipleDSConfiguration {
    private Map<String, Map<String,String>> ds;
    private int initialSize;
    private int minIdle;
    private int maxActive;
    private int maxWait;
    private int timeBetweenEvictionRunsMillis;
    private int minEvictableIdleTimeMillis;
    private int maxEvictableIdleTimeMillis;
    private String validationQuery;
    private boolean testOnBorrow;
    private boolean testOnReturn;
    private boolean testWhileIdle;

    public DruidDataSource dataSource(DruidDataSource druidDataSource){
        // 初始连接数
        druidDataSource.setInitialSize(initialSize);
        // 最小连接池数量
        druidDataSource.setMinIdle(minIdle);
        // 最大连接池数量
        druidDataSource.setMaxActive(maxActive);
        // 获取连接等待超时的时间
        druidDataSource.setMaxWait(maxWait);
        // 检测间隔时间，检测需要关闭的空闲连接，单位毫秒
        druidDataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        // 一个连接在连接池中最小的生存时间，单位毫秒
        druidDataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        // 一个连接在连接池中最大的生存时间，单位毫秒
        druidDataSource.setMaxEvictableIdleTimeMillis(maxEvictableIdleTimeMillis);
        // 配置检测连接是否有效
        druidDataSource.setValidationQuery(validationQuery);
        // 如果为true（默认为false），当应用向连接池申请连接时，连接池会判断这条连接是否是可用的
        druidDataSource.setTestOnBorrow(testOnBorrow);
        // 连接返回检测
        druidDataSource.setTestOnReturn(testOnReturn);
        // 失效连接检测
        druidDataSource.setTestWhileIdle(testWhileIdle);
        return druidDataSource;
    }

}
```
> 读取yaml文件数据进行配置，其中ds读取yaml文件中的ds属性，其他属性同理

## 3.4 加载数据源
MultipleDataSourceProvider 多数据源提供者接口
```java
import javax.sql.DataSource;
import java.util.Map;

/**
 * <h1>多数据源提供者</h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */
public interface MultipleDataSourceProvider {

    String DEFAULT_DATASOURCE = "master";

    Map<String, DataSource> loadDataSource();
}
```
YmlMultipleDataSourceProvider  多数据源提供者Yaml实现
```java
import cn.flowboot.multisource.config.MultipleDSConfiguration;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * <h1>yaml 数据配置加载实现</h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */
@Component
@Configuration
public class YmlMultipleDataSourceProvider implements MultipleDataSourceProvider{
    @Autowired
    private MultipleDSConfiguration multipleDSConfiguration;

    @Override
    public Map<String, DataSource> loadDataSource() {
        Map<String, Map<String, String>> myDS = multipleDSConfiguration.getDs();
        Map<String,DataSource> map = new HashMap<>(myDS.size());
        try{
            for (String key: myDS.keySet()){
                DruidDataSource druidDataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(myDS.get(key));
                map.put(key,multipleDSConfiguration.dataSource(druidDataSource));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return map;
    }
}
```

> 从MultipleDSConfiguration配置类中获取DS数据Map，将Map数据转化为DataSource的Map数据返回出去

## 3.5 切换数据源
DynamicMultipleDataSourceContextHolder

```java
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * <h1>多数据源线程池</h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */
@Data
@Slf4j
public class DynamicMultipleDataSourceContextHolder {

    private String dataSourceName;

    private static final ThreadLocal<String> CURRENT_DATASOURCE_NAME = new ThreadLocal<>();

    public static void setDataSourceName(String dataSourceName){
        log.info("切换到{}数据源",dataSourceName);
        CURRENT_DATASOURCE_NAME.set(dataSourceName);
    }

    public static String getDataSourceName(){
        return CURRENT_DATASOURCE_NAME.get();
    }

    public static void clearDataSourceName(){
        CURRENT_DATASOURCE_NAME.remove();
    }
}
```
> 对于当前数据库操作应当使用哪个数据源有多种实现方式，需要说明的是当前数据库操作对数据源所做的修改不应该影响到其他的数据库操作，因此可以使用ThreadLocal来实现。将当前数据库操作所使用的数据源存入到ThreadLocal中，这样只有当前线程才能获取到该数据，保证了多线程并发情况下数据的安全性。

- 首先定义一个用于操作ThreadLocal的类DynamicMultipleDataSourceContextHolder
- 主要用于往ThreadLocal中存入、获取和清除数据

## 3.6 标记数据源
MultipleDataSource 注解定义

```java
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h1>标记使用数据源的名称</h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */

@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MultipleDataSource {

    String dataSourceName() default MultipleDataSourceProvider.DEFAULT_DATASOURCE;

    @AliasFor("dataSourceName")
    String value() default MultipleDataSourceProvider.DEFAULT_DATASOURCE;
}
```
## 3.7 解析自定义注解
DataSourceAspect  通过AOP来解析该自定义注解
```java
package cn.flowboot.multisource.source;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * <h1>切面实现切换数据源</h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */
@Order(1)
@Aspect
@Component
public class DataSourceAspect {

    @Pointcut("@annotation(cn.flowboot.multisource.source.MultipleDataSource)"
            +"||@within(cn.flowboot.multisource.source.MultipleDataSource)")
    public void myDS(){};

    @Around("myDS()")
    public Object around(ProceedingJoinPoint point)throws Throwable {
        MethodSignature signature = (MethodSignature)point.getSignature();
        MultipleDataSource multipleDataSource = AnnotationUtils.findAnnotation(signature.getMethod(), MultipleDataSource.class);
        if(Objects.nonNull(multipleDataSource)){
            DynamicMultipleDataSourceContextHolder.setDataSourceName(multipleDataSource.dataSourceName());
        }
        try{
            return point.proceed();
        } finally {
            // 清空数据源
            DynamicMultipleDataSourceContextHolder.clearDataSourceName();
        }
    }
}
```
主要逻辑：
- 1.AnnotationUtils找到当前方法上MultipleDataSource注解数据
- 2.如果存在提供多数据源上下文设置数据源
- 3.代理调用方法
- 4.清除数据源

## 3.8 动态使用数据源
DynamicMultipleDataSource 继承于AbstractRoutingDataSource抽象类并重写其中的determineTargetDataSource()方法。
```java
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.HashMap;
import java.util.Map;

/**
 * <h1>动态使用数据源</h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */
public class DynamicMultipleDataSource extends AbstractRoutingDataSource {
    //实际数据源提供者
    private YmlMultipleDataSourceProvider ymlMultipleDataSourceProvider;

    public DynamicMultipleDataSource(YmlMultipleDataSourceProvider provider){
        this.ymlMultipleDataSourceProvider = provider;
        Map<Object, Object> targetDataSources = new HashMap<>(provider.loadDataSource());
        super.setTargetDataSources(targetDataSources);
        super.setDefaultTargetDataSource(provider.loadDataSource().get(MultipleDataSourceProvider.DEFAULT_DATASOURCE));
        super.afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String dataSourceName = DynamicMultipleDataSourceContextHolder.getDataSourceName();
        return dataSourceName;
    }
}
```

## 3.9 注解Bean
将DynamicMultipleDataSource注入到Spring容器中

```java
import cn.flowboot.multisource.source.DynamicMultipleDataSource;
import cn.flowboot.multisource.source.YmlMultipleDataSourceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <h1>动态数据原配置</h1>
 *  创建多数据源，将Yaml配置读取传递给动态多数据源
 * @author Vincent Vic
 * @version 1.0
 */
@Configuration
public class DynamicMultipleDataSourceConfiguration {
    @Autowired
    private YmlMultipleDataSourceProvider provider;

    @Bean
    public DynamicMultipleDataSource dynamicMultipleDataSource(){
        return new DynamicMultipleDataSource(provider);
    }
}

```

# 4. 测试
## 4.1 数据库准备
分别创建ds1和ds2 都创建good 数据表，数据自行填充
```sql
CREATE TABLE `good` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `price` int(11) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 4.2 dao
```java
@Mapper
public interface GoodMapper {
    @Select("select count(*) from good")
    Integer number();
}

```
## 4.2 service
```java
@RequiredArgsConstructor
@Service
public class GoodService {
    private final GoodMapper goodMapper;

    @MultipleDataSource("master")
    public Integer master(){
        return goodMapper.number();
    }

    @MultipleDataSource("slave")
    public Integer slave(){
        return goodMapper.number();
    }
}
```
## 4.3 controller

```java
@RequiredArgsConstructor
@RestController
@Slf4j
public class GoodController {

    private final GoodService goodService;

    @GetMapping("/good")
    public List<Integer> books(){
        List<Integer> list = new ArrayList<>();
        log.info("master db numbers is {}",goodService.master());
        list.add(goodService.master());
        log.info("slave db numbers is {}",goodService.slave());
        list.add(goodService.slave());
        return list;
    }
}
```
## 4.4 执行测试
启动项目进行测试。打开浏览器，访问 http://localhost:8080/good 链接，可以看到它显示一个列表[1,2]，而IDEA控制台输出如下信息：

![在这里插入图片描述](https://img-blog.csdnimg.cn/54080f2d328c4aaba650859c707b709b.png)

案例：[Github](https://github.com/Vincent-Vic/spring-learning-cases/tree/master/multi-source)
