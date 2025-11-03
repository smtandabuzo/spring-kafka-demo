package com.example.kafkademo.service;

import com.example.kafkademo.events.interfaces.Event;
import com.example.kafkademo.enums.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventLogService {
    
    private final EventStorageService eventStorageService;

    public void logEvent(Event event) {
        try {
            eventStorageService.saveEvent(event);
            log.debug("Successfully logged event: {} for user {}", 
                    event.getEventType(), event.getUserId());
        } catch (Exception e) {
            log.error("Failed to log event: {}", event, e);
            // Consider implementing a dead letter queue or retry mechanism here
        }
    }

    public List<Event> getEventsByUserId(String userId) {
        try {
            return eventStorageService.getEventsByUserId(userId);
        } catch (Exception e) {
            log.error("Failed to get events for user: {}", userId, e);
            throw new RuntimeException("Failed to retrieve events", e);
        }
    }

    public List<Event> getEventsByUserIdAndType(String userId, EventType eventType) {
        try {
            return eventStorageService.getEventsByUserIdAndType(userId, eventType);
        } catch (Exception e) {
            log.error("Failed to get events for user: {} and type: {}", userId, eventType, e);
            throw new RuntimeException("Failed to retrieve events", e);
        }
    }
    
    public void clearEvents() {
        try {
            eventStorageService.clearEvents();
            log.info("All events have been cleared from storage");
        } catch (Exception e) {
            log.error("Failed to clear events", e);
            throw new RuntimeException("Failed to clear events", e);
        }
    }
}
