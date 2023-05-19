package com.github.shibadog.sample.springboot3stream;

import java.util.Map;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class SampleController {

    private static final String exchange = "listener";

    private final StreamBridge streamBridge;

    public SampleController(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @PostMapping(value="/post")
    public String post(@RequestBody Map<String, Object> param) {
        streamBridge.send(exchange, param, MimeTypeUtils.APPLICATION_JSON);
        return "OK";
    }
    
}
