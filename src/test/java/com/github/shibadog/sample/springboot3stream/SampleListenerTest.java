package com.github.shibadog.sample.springboot3stream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.messaging.support.GenericMessage;

@SpringBootTest
public class SampleListenerTest {
    
    @Autowired
    private InputDestination input;

    // @SpyBean
    // @Qualifier("consume")
    // private Consumer<Message<Map<String, Object>>> consume;

    @Test
    void consume_test() {
        this.input.send(new GenericMessage<byte[]>("{ \"message\": \"test\" }".getBytes()), "listener");

        // verify(consume, times(1)).accept(any());
    }
}
