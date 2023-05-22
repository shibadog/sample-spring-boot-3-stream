package com.github.shibadog.sample.springboot3stream;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.cloud.function.context.DefaultMessageRoutingHandler;
import org.springframework.cloud.function.context.MessageRoutingCallback;
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

    // ##################################################################

    /** ルーティング設定
     * @StreamListenerのconditionフィールドを再現している。
     */
    @Bean
    MessageRoutingCallback customRouter() {
        return new MessageRoutingCallback() {
            @Override
            public String routingResult(Message<?> message) {
                return Optional.ofNullable(message.getHeaders().get("path"))
                        .map(Object::toString)
                        .map(p -> Arrays.asList(p.split("/"))).map(LinkedList::new)
                        .map(LinkedList::getLast)
                        .orElseGet(() -> MessageRoutingCallback.super.routingResult(message));
            }
        };
    }

    @Bean
    Consumer<Message<Map<String, Object>>> hoge() {
        return this::routingConsumeHogeFnc;
    }

    @Bean
    Consumer<Message<Map<String, Object>>> fuga() {
        return this::routingConsumeFugaFnc;
    }

    void routingConsumeHogeFnc(Message<Map<String, Object>> message) {
        log.info("hoge header: {}", message.getHeaders());
        log.info("hoge receive message!! {}", message.getPayload());
    }
    void routingConsumeFugaFnc(Message<Map<String, Object>> message) {
        log.info("fuga header: {}", message.getHeaders());
        log.info("fuga receive message!! {}", message.getPayload());
    }

    /** どこにも行かなかったら、ここに落ちる。 */
    @Bean
    DefaultMessageRoutingHandler defaultRoutingHandler() {
        return new DefaultMessageRoutingHandler() {
            @Override
            public void accept(Message<?> message) {
                log.info("no routing header: {}", message.getHeaders());
                log.info("no routing receive message!! {}", message.getPayload());
            }
        };
    }
}
