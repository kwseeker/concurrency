package top.kwseeker.concurrency.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
public class EntityController {

//    Logger logger = LoggerFactory.getLogger(this.getClass()); //可以使用lombok @Slf4j代替

    //HttpEntity类似于@requestbody和@responsebody。除了访问请求和响应主体之外，
    //HttpEntity（以及响应特定的子类ResponseEntity）还允许访问请求和响应头，如下所示：
    // TODO: 请求头 Header 的作用详解
    @RequestMapping("/testEntity")
    public ResponseEntity<String> handle(HttpEntity<byte[]> requestEntity) {
        String requestHeader = requestEntity.getHeaders().getFirst("MyRequestHeader");
        byte[] requestBody = requestEntity.getBody();

//        logger.info("requestHeader: {}\nrequestBody: {}", requestHeader, requestBody);
        log.info("requestHeader: {}\nrequestBody: {}", requestHeader, requestBody);

        // do something with request header and body

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("MyResponseHeader", "MyValues");
        return new ResponseEntity<>("Hello, this message is from EntityController",
                responseHeaders, HttpStatus.CREATED);
    }
}
