package com.example.kafkademo.service;

import com.example.kafkademo.events.interfaces.Event;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class EventLogService {
    private final Map<String, List<Event>> userEvents = new ConcurrentHashMap<>();
    private final Set<String> processedEventIds = Collections.synchronizedSet(new HashSet<>());

    @KafkaListener(topics = "user-events", groupId = "event-log-service")
    public void consume(Event event) {
        if (event == null || event.getEventId() == null || processedEventIds.contains(event.getEventId())) {
            return; // Skip processing if event is null, has no ID, or was already processed
        }
        
        String userId = event.getUserId();
        if (userId != null) {
            userEvents.computeIfAbsent(userId, k -> Collections.synchronizedList(new ArrayList<>())).add(event);
            processedEventIds.add(event.getEventId());
        }
    }

    public List<Event> getEventsByUserId(String userId) {
        List<Event> events = userEvents.getOrDefault(userId, Collections.emptyList());
        return new ArrayList<>(events); // Return a copy to avoid concurrent modification
    }

    public List<Event> getEventsByUserIdAndType(String userId, String eventType) {
        return userEvents.getOrDefault(userId, Collections.emptyList()).stream()
                .filter(event -> eventType.equals(event.getEventType()))
                .collect(Collectors.toList());
    }

    public void clearEvents() {
        userEvents.clear();
        processedEventIds.clear();
    }
}
