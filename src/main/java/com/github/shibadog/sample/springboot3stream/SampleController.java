package com.github.shibadog.sample.springboot3stream;

import java.util.Map;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PathVariable;
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

    @PostMapping(value="/routing/{key}")
    public String routing(@PathVariable(required = true) String key,
            @RequestBody Map<String, Object> body) {
        streamBridge.send(exchange,
                MessageBuilder.withPayload(body)
                        .setHeader("path", "/service/" + key)
                        .build(),
                MimeTypeUtils.APPLICATION_JSON);
        return "OK";
    }
    
}
