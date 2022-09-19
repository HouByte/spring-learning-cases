package cn.meshed.multisource.source;

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