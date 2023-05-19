package com.github.shibadog.sample.springboot3stream;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class SampleListener {

    @Bean
    Consumer<Message<Map<String, Object>>> consume() {
        return this::consumeFnc;
    }

    void consumeFnc(Message<Map<String, Object>> message) {
        switch (Objects.toString(message.getPayload().getOrDefault("type", null))) {
            case "exception":
                throw new RuntimeException("exception");
            default:
                log.info("header: {}", message.getHeaders());
                log.info("receive message!! {}", message.getPayload());
        }
    }
}
