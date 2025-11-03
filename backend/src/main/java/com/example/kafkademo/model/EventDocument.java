package com.example.kafkademo.model;

import com.example.kafkademo.enums.EventType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.Map;

@Document(collection = "user-events")
@Data
public class EventDocument {
    @Id
    private String id;
    
    @Field("event_id")
    private String eventId;
    
    @Field("user_id")
    private String userId;
    
    @Field("event_type")
    private EventType eventType;
    
    @Field("timestamp")
    private long timestamp;
    
    @Field("properties")
    private Map<String, Object> properties;
    
    @Field("created_at")
    private Instant createdAt;
    
    // Default constructor for MongoDB
    public EventDocument() {
        this.createdAt = Instant.now();
    }
    
    public EventDocument(String eventId, String userId, EventType eventType, 
                        long timestamp, Map<String, Object> properties) {
        this();
        this.eventId = eventId;
        this.userId = userId;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.properties = properties;
    }
}
