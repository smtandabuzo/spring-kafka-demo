package com.example.kafkademo.consumer;

import com.example.kafkademo.avro.UserEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventConsumer {

    @KafkaListener(topics = "user-events", groupId = "user-event-consumers")
    public void consume(UserEvent event) {
        System.out.println("📩 Received user event: " + event);
    }
}
