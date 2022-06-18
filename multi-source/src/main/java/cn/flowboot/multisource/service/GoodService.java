package cn.flowboot.multisource.service;

import cn.flowboot.multisource.mapper.GoodMapper;
import cn.flowboot.multisource.source.MyDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <h1></h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */
@RequiredArgsConstructor
@Service
public class GoodService {
    private final GoodMapper goodMapper;

    @MyDataSource("master")
    public Integer master(){
        return goodMapper.number();
    }

    @MyDataSource("slave")
    public Integer slave(){
        return goodMapper.number();
    }
}
