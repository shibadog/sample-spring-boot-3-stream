package com.github.shibadog.sample.springboot3stream;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.springframework.cloud.function.context.DefaultMessageRoutingHandler;
import org.springframework.cloud.function.context.MessageRoutingCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.GlobalChannelInterceptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
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
    Consumer<Message<Map<String, Object>>> hoge(Metadata metadata) {
        return m -> handle(m, metadata, "hoge");
    }

    @Bean
    Consumer<Message<Map<String, Object>>> fuga(Metadata metadata) {
        return m -> handle(m, metadata, "fuga");
    }

    void handle(Message<Map<String, Object>> message, Metadata metadata, String function) {
        metadata.uuid(UUID.randomUUID().toString());
        log.info("metadata: {}", metadata);
        try {
            TimeUnit.SECONDS.sleep(5L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        switch(Objects.toString(message.getPayload().get("type"))) {
            case "exception":
                throw new RuntimeException("error!!!!" + metadata.toString());
            default:
                log.info("{} header: {} metadata: {}", function, message.getHeaders(), metadata);
                log.info("{} receive message!! {} metadata: {}", function, message.getPayload(), metadata);
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
                RequestContextHolder.setRequestAttributes(new RequestScopeAttributes());
                return ChannelInterceptor.super.preSend(message, channel);
            }
            @Override
            public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
                log.info("postSend channel:{}", channel.toString());
                RequestContextHolder.resetRequestAttributes();
                ChannelInterceptor.super.postSend(message, channel, sent);
            }
            @Override
            public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
                log.info("afterSendCompletion channel:{}", channel.toString());
                ChannelInterceptor.super.afterSendCompletion(message, channel, sent, ex);
            }
        };
    }

    @RequestScope
    @Component
    @Accessors(fluent = true)
    @ToString @EqualsAndHashCode
    public static class Metadata {
        @Getter @Setter private String uuid;
    }

    public static class RequestScopeAttributes implements RequestAttributes {
        private Map<String, Object> requestAttributeMap = new HashMap<>();

        @Override
        public Object getAttribute(String name, int scope) {
            if (scope == RequestAttributes.SCOPE_REQUEST) {
                return this.requestAttributeMap.get(name);
            }
            return null;
        }
    
        @Override
        public void setAttribute(String name, Object value, int scope) {
            if (scope == RequestAttributes.SCOPE_REQUEST) {
                this.requestAttributeMap.put(name, value);
            }
        }
    
        @Override
        public void removeAttribute(String name, int scope) {
            if (scope == RequestAttributes.SCOPE_REQUEST) {
                this.requestAttributeMap.remove(name);
            }
        }
    
        @Override
        public String[] getAttributeNames(int scope) {
            if (scope == RequestAttributes.SCOPE_REQUEST) {
                return this.requestAttributeMap.keySet().toArray(new String[0]);
            }
            return new String[0];
        }
    
        @Override
        public void registerDestructionCallback(String name, Runnable callback, int scope) {
            // Not Supported
        }
    
        @Override
        public Object resolveReference(String key) {
            // Not supported
            return null;
        }
    
        @Override
        public String getSessionId() {
            return null;
        }
    
        @Override
        public Object getSessionMutex() {
            return null;
        }
    }
}
