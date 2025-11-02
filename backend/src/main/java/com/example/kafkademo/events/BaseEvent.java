package com.example.kafkademo.events;

import com.example.kafkademo.events.interfaces.Event;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@SuperBuilder
public abstract class BaseEvent implements Event {
    protected String eventId;
    protected String userId;
    protected long timestamp;
    protected Map<String, String> properties;

    protected BaseEvent(String userId) {
        this();
        this.userId = userId;
    }
    
    {
        // Instance initializer block - runs for all constructors
        if (eventId == null) {
            eventId = UUID.randomUUID().toString();
        }
        if (timestamp == 0) {
            timestamp = Instant.now().toEpochMilli();
        }
        if (properties == null) {
            properties = new HashMap<>();
        }
    }

    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    public String getProperty(String key) {
        return properties.get(key);
    }
}
