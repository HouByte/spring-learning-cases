package cn.flowboot.multisource.source;

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
