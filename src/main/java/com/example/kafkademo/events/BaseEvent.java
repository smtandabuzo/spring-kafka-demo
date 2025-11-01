package com.example.kafkademo.events;

import com.example.kafkademo.events.interfaces.Event;
import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public abstract class BaseEvent implements Event {
    protected String eventId;
    protected String userId;
    protected long timestamp;
    protected Map<String, String> properties;

    protected BaseEvent(String userId) {
        this.eventId = UUID.randomUUID().toString();
        this.userId = userId;
        this.timestamp = Instant.now().toEpochMilli();
        this.properties = new HashMap<>();
    }

    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    public String getProperty(String key) {
        return properties.get(key);
    }
}
