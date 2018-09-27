package top.kwseeker.concurrency.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController     //这个注解是 @Controller和@ResponseBody的组合注解，本身属于spring-web包，SpringBoot中整合进了spring-boot-starter-web
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "test";
    }

}
