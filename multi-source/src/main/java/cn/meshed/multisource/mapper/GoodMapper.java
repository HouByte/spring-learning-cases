package cn.meshed.multisource.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * <h1></h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */
@Mapper
public interface GoodMapper {
    @Select("select count(*) from good")
    Integer number();
}
