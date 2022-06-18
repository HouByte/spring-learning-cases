package cn.flowboot.multisource.controller;

import cn.flowboot.multisource.service.GoodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1></h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */
@RequiredArgsConstructor
@RestController
@Slf4j
public class GoodController {

    private final GoodService goodService;

    @GetMapping("/good")
    public List<Integer> books(){
        List<Integer> list = new ArrayList<>();
        log.info("master db numbers is {}",goodService.master());
        list.add(goodService.master());
        log.info("slave db numbers is {}",goodService.slave());
        list.add(goodService.slave());
        return list;
    }
}
