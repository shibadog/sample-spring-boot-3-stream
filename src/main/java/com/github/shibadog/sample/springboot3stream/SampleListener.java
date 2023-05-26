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
import org.springframework.integration.config.GlobalChannelInterceptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;

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

    @Bean
    @GlobalChannelInterceptor(patterns = "input") // bindingNameのほうを指定する必要がある！
    ChannelInterceptor customInterceptor() {
        return new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                log.info("preSend channel:{}", channel.toString());
                return ChannelInterceptor.super.preSend(message, channel);
            }
            @Override
            public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
                log.info("postSend channel:{}", channel.toString());
                ChannelInterceptor.super.postSend(message, channel, sent);
            }
            @Override
            public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
                log.info("afterSendCompletion channel:{}", channel.toString());
                ChannelInterceptor.super.afterSendCompletion(message, channel, sent, ex);
            }
        };
    }
}
