package cn.flowboot.multisource.service;

import cn.flowboot.multisource.mapper.GoodMapper;
import cn.flowboot.multisource.source.MultipleDataSource;
import lombok.RequiredArgsConstructor;
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

    @MultipleDataSource("master")
    public Integer master(){
        return goodMapper.number();
    }

    @MultipleDataSource("slave")
    public Integer slave(){
        return goodMapper.number();
    }
}
