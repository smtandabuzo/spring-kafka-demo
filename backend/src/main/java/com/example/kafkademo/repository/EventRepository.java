package com.example.kafkademo.repository;

import com.example.kafkademo.enums.EventType;
import com.example.kafkademo.model.EventDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends MongoRepository<EventDocument, String> {
    
    List<EventDocument> findByUserId(String userId);
    
    List<EventDocument> findByUserIdAndEventType(String userId, EventType eventType);
    
    List<EventDocument> findByEventType(EventType eventType);
    
    List<EventDocument> findByTimestampBetween(long startTime, long endTime);
}
