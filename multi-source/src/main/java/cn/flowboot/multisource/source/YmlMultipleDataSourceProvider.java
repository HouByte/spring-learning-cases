package cn.flowboot.multisource.source;

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