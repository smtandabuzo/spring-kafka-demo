package com.example.kafkademo.controller;

import com.example.kafkademo.enums.EventType;
import com.example.kafkademo.events.interfaces.Event;
import com.example.kafkademo.factory.EventFactory;
import com.example.kafkademo.processor.EventProcessorManager;
import com.example.kafkademo.producer.UserEventProducer;
import com.example.kafkademo.product.ItemContext;
import com.example.kafkademo.user.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final KafkaTemplate<String, Event> kafkaTemplate;
    private final EventProcessorManager eventProcessorManager;

    @PostMapping
    public ResponseEntity<String> trackEvent(
            @RequestParam String userId,
            @RequestParam String eventType,
            @RequestBody Map<String, Object> eventData) {

        try {
            Event event = createEvent(userId, eventType, eventData);
            kafkaTemplate.send("user-events", event.getEventId(), event);

            // Process event synchronously (or could be async)
            eventProcessorManager.processEvent(event);

            return ResponseEntity.ok("Event tracked successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid event type: " + e.getMessage());
        }
    }

    private Event createEvent(String userId, String eventType, Map<String, Object> data) {
        EventType type = EventType.valueOf(eventType.toUpperCase());

        switch (type) {
            case PAGE_VIEW:
                return EventFactory.createPageView(
                        createUserContext(data),
                        (String) data.get("pageUrl"),
                        (String) data.get("pageTitle")
                );

            case ADD_TO_CART:
                ItemContext item = createItemContext((Map<String, Object>) data.get("item"));
                return EventFactory.createAddToCart(
                        userId,
                        item,
                        (int) data.getOrDefault("quantity", 1)
                );

            case PURCHASE:
                List<ItemContext> items = ((List<Map<String, Object>>) data.get("items")).stream()
                        .map(this::createItemContext)
                        .collect(Collectors.toList());

                return EventFactory.createPurchase(
                        userId,
                        (String) data.get("orderId"),
                        items,
                        (String) data.get("paymentMethod")
                );

            default:
                throw new IllegalArgumentException("Unsupported event type: " + eventType);
        }
    }

    // Helper methods to create context objects from maps
    private UserContext createUserContext(Map<String, Object> data) {
        return UserContext.builder()
                .sessionId((String) data.get("sessionId"))
                .userAgent((String) data.get("userAgent"))
                .ipAddress((String) data.get("ipAddress"))
                .build();
    }

    private ItemContext createItemContext(Map<String, Object> data) {
        return ItemContext.builder()
                .itemId((String) data.get("itemId"))
                .itemType((String) data.get("itemType"))
                .price(((Number) data.get("price")).doubleValue())
                .currency((String) data.get("currency"))
                .quantity((int) data.getOrDefault("quantity", 1))
                .build();
    }
}