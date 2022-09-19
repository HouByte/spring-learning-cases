package cn.meshed.boot.service.impl;

import cn.meshed.boot.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1></h1>
 *
 * @author hougq
 * @version 1.0
 */
@RequiredArgsConstructor
@Service
public class TestServiceImpl implements TestService {
    @Override
    public List<Integer> list() {
        return new ArrayList<Integer>(){{
            add(1);
            add(2);
            add(3);
            add(4);
            add(5);
        }};
    }
}
