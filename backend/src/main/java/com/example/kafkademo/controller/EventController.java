package com.example.kafkademo.controller;

import com.example.kafkademo.enums.EventType;
import com.example.kafkademo.events.interfaces.Event;
import com.example.kafkademo.factory.EventFactory;
import com.example.kafkademo.processor.EventProcessorManager;
import com.example.kafkademo.product.ItemContext;
import com.example.kafkademo.user.UserContext;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.http.HttpStatus;
import com.example.kafkademo.service.EventLogService;
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final KafkaTemplate<String, Event> kafkaTemplate;
    private final EventProcessorManager eventProcessorManager;
    private final EventLogService eventLogService;
    
    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @GetMapping
    public ResponseEntity<?> getEvents(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String eventType) {
        
        List<Event> events = new ArrayList<>();
        String message = "No matching events found";
        
        if (userId != null && !userId.isEmpty()) {
            if (eventType != null && !eventType.isEmpty()) {
                events = eventLogService.getEventsByUserIdAndType(userId, eventType);
            } else {
                events = eventLogService.getEventsByUserId(userId);
            }
            
            if (events == null || events.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", message));
            }
        } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "No events found"));
        }
        
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/clear")
    public ResponseEntity<String> clearEventLog() {
        eventLogService.clearEvents();
        return ResponseEntity.ok("Event log cleared successfully");
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EventController.class);

    @PostMapping(produces = "application/json")
    public ResponseEntity<?> trackEvent(@RequestBody Map<String, Object> request) {
        log.info("Received trackEvent request: {}", request);
        if (request == null) {
            log.error("Request body is null");
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Request body cannot be null"));
        }
        
        try {
            // Extract common fields
            String userId = (String) request.get("userId");
            String eventTypeStr = (String) request.get("eventType");
            
            if (userId == null || eventTypeStr == null) {
                return ResponseEntity.badRequest().body("Missing required fields: userId and eventType are required");
            }
            
            String eventType = eventTypeStr.toUpperCase();
            String sessionId = (String) request.get("sessionId");
            String userAgent = (String) request.get("userAgent");
            String ipAddress = (String) request.get("ipAddress");

            // Create base event data
            Map<String, Object> eventData = new java.util.HashMap<>();
            eventData.put("sessionId", sessionId);
            eventData.put("userAgent", userAgent);
            eventData.put("ipAddress", ipAddress);

            // Handle different event types
            switch (EventType.valueOf(eventType)) {
                case PAGE_VIEW:
                    eventData.put("pageUrl", request.get("pageUrl"));
                    eventData.put("pageTitle", request.get("pageTitle"));
                    break;
                    
                case ADD_TO_CART:
                    Map<String, Object> item = new java.util.HashMap<>();
                    item.put("itemId", request.get("itemId"));
                    item.put("itemType", request.get("itemType"));
                    item.put("price", request.get("price"));
                    item.put("quantity", request.get("quantity") != null ? request.get("quantity") : 1);
                    eventData.put("item", item);
                    break;
                    
                case PURCHASE:
                    eventData.put("orderId", request.get("orderId"));
                    eventData.put("paymentMethod", request.get("paymentMethod"));
                    eventData.put("currency", request.get("currency"));
                    
                    List<Map<String, Object>> items = new ArrayList<>();
                    // For purchase, we expect items to be sent as an array in the request
                    if (request.get("items") != null && request.get("items") instanceof List) {
                        items = (List<Map<String, Object>>) request.get("items");
                    } else if (request.get("itemId") != null) {
                        // For backward compatibility, create an item from the form fields
                        Map<String, Object> purchaseItem = new HashMap<>();
                        purchaseItem.put("itemId", request.get("itemId"));

                        // Set itemType with default if not provided
                        purchaseItem.put("itemType", request.get("itemType") != null ? request.get("itemType") : "UNKNOWN");
                        
                        // Validate and set price with default 0.0 if not provided
                        try {
                            BigDecimal price = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                            if (request.get("price") != null) {
                                price = new BigDecimal(request.get("price").toString())
                                        .setScale(2, RoundingMode.HALF_UP);
                                if (price.compareTo(BigDecimal.ZERO) < 0) {
                                    throw new IllegalArgumentException("Price cannot be negative");
                                }
                            }
                            purchaseItem.put("price", price.doubleValue());
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Invalid price format. Please provide a valid number.", e);
                        }
                        
                        // Set quantity with default 1 if not provided
                        int quantity = 1;
                        if (request.get("quantity") != null) {
                            try {
                                quantity = Integer.parseInt(request.get("quantity").toString());
                                if (quantity < 1) {
                                    throw new IllegalArgumentException("Quantity must be at least 1");
                                }
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException("Invalid quantity format", e);
                            }
                        }
                        purchaseItem.put("quantity", quantity);
                        
                        items.add(purchaseItem);
                    }
                    eventData.put("items", items);
                    break;
            }

            // Create the event
            log.debug("Creating event with userId: {}, eventType: {}", userId, eventType);
            Event event = createEvent(userId, eventType, eventData);
            log.debug("Created event: {}", event);
            
            try {
                // Send to Kafka
                log.debug("Sending event to Kafka");
                kafkaTemplate.send("user-events", event.getEventId(), event)
                    .addCallback(
                        result -> log.debug("Successfully sent message to topic: {}", result != null ? result.getRecordMetadata().topic() : "unknown"),
                        ex -> log.error("Unable to send message to Kafka: {}", ex.getMessage(), ex)
                    );

                // Process event synchronously
                log.debug("Processing event");
                eventProcessorManager.processEvent(event);
                log.debug("Successfully processed event");
            } catch (Exception e) {
                log.error("Error processing/sending event: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to process or send event: " + e.getMessage(), e);
            }

            return ResponseEntity.ok(Collections.singletonMap("message", "Event tracked successfully"));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid event type: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid event type");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", new Date().toString());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Error processing event: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to process event");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", new Date().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
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
                .price(BigDecimal.valueOf(((Number) data.get("price")).doubleValue()))
                .currency((String) data.get("currency"))
                .quantity((int) data.getOrDefault("quantity", 1))
                .build();
    }
}