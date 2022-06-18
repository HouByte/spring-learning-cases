package cn.flowboot.multisource.source;

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
public @interface MyDataSource {

    String dataSourceName() default MultipleDataSourceProvider.DEFAULT_DATASOURCE;

    @AliasFor("dataSourceName")
    String value() default MultipleDataSourceProvider.DEFAULT_DATASOURCE;
}