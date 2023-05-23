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
        return m -> handle(m, "hoge");
    }

    @Bean
    Consumer<Message<Map<String, Object>>> fuga() {
        return m -> handle(m, "fuga");
    }

    void handle(Message<Map<String, Object>> message, String function) {
        switch(Objects.toString(message.getPayload().get("type"))) {
            case "exception":
                throw new RuntimeException("error!!!!");
            default:
                log.info("{} header: {}", function, message.getHeaders());
                log.info("{} receive message!! {}", function, message.getPayload());
        }
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
