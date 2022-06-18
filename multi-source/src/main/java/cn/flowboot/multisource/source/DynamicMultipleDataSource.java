package cn.flowboot.multisource.source;

/**
 * <h1>动态使用数据源</h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.HashMap;
import java.util.Map;


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