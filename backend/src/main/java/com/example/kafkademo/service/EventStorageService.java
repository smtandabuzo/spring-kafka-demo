package com.example.kafkademo.service;

import com.example.kafkademo.enums.EventType;
import com.example.kafkademo.events.interfaces.Event;
import com.example.kafkademo.model.EventDocument;
import com.example.kafkademo.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventStorageService {

    private final EventRepository eventRepository;

    public void saveEvent(Event event) {
        try {
            // Convert Map<String, String> to Map<String, Object>
            Map<String, Object> properties = new HashMap<>(event.getProperties());
            
            EventDocument doc = new EventDocument(
                event.getEventId(),
                event.getUserId(),
                event.getEventType(),
                event.getTimestamp(),
                properties
            );
            eventRepository.save(doc);
            log.debug("Saved event to MongoDB: {} - {}", event.getEventType(), event.getEventId());
        } catch (Exception e) {
            log.error("Failed to save event to MongoDB: {}", event, e);
            // Consider implementing a dead letter queue or retry mechanism here
        }
    }

    public List<Event> getEventsByUserId(String userId) {
        return eventRepository.findByUserId(userId).stream()
            .map(this::mapToEvent)
            .collect(Collectors.toList());
    }

    public List<Event> getEventsByUserIdAndType(String userId, EventType eventType) {
        return eventRepository.findByUserIdAndEventType(userId, eventType).stream()
            .map(this::mapToEvent)
            .collect(Collectors.toList());
    }
    
    public void clearEvents() {
        try {
            eventRepository.deleteAll();
            log.info("All events have been cleared from the database");
        } catch (Exception e) {
            log.error("Failed to clear events from the database", e);
            throw new RuntimeException("Failed to clear events from the database", e);
        }
    }

    private Event mapToEvent(EventDocument doc) {
        // This is a simplified mapping. You might need to implement a proper mapping
        // based on your event types and their specific properties.
        return new Event() {
            @Override
            public String getEventId() {
                return doc.getEventId();
            }

            @Override
            public String getUserId() {
                return doc.getUserId();
            }

            @Override
            public long getTimestamp() {
                return doc.getTimestamp();
            }

            @Override
            public Map<String, String> getProperties() {
                // Convert Map<String, Object> to Map<String, String>
                return doc.getProperties().entrySet().stream()
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue() != null ? e.getValue().toString() : ""
                    ));
            }

            @Override
            public EventType getEventType() {
                return doc.getEventType();
            }
        };
    }
}
