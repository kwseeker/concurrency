package top.kwseeker.concurrency.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import top.kwseeker.concurrency.concurrent_module.thread_confinement.RequestHolder;

@Slf4j
@Controller
@RequestMapping("/threadLocal")
public class ThreadLocalController {

    //模拟ThreadLocal的使用，在Filter中赋值，Controller中读取，请求返回后清除
    @RequestMapping("/test")
    @ResponseBody
    public Long test() {
        log.info("in controller /threadLocal/test");
        return RequestHolder.getId();
    }
}
