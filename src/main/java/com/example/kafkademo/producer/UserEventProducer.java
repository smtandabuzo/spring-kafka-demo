package com.example.kafkademo.producer;

import com.example.kafkademo.events.interfaces.Event;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserEventProducer {

    private static final String TOPIC = "user-events";
    private final KafkaTemplate<String, Event> kafkaTemplate;

    public UserEventProducer(KafkaTemplate<String, Event> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String key, Event message) {
        kafkaTemplate.send(TOPIC, key, message);
    }
}