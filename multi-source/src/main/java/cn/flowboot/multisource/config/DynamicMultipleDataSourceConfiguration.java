package cn.flowboot.multisource.config;

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
