package com.example.kafkademo.processor;

import com.example.kafkademo.enums.EventType;
import com.example.kafkademo.events.PurchaseEvent;
import com.example.kafkademo.events.interfaces.Event;
import com.example.kafkademo.events.interfaces.EventProcessor;
import org.springframework.stereotype.Service;

@Service
public class PurchaseEventProcessor implements EventProcessor {
    @Override
    public boolean canProcess(Event event) {
        return event.getEventType() == EventType.PURCHASE;
    }

    @Override
    public void process(Event event) {
        if (!(event instanceof PurchaseEvent)) return;
        PurchaseEvent purchase = (PurchaseEvent) event;

        // Process purchase (e.g., update inventory, send confirmation email, etc.)
        System.out.println("Processing purchase: " + purchase.getOrderId());
    }
}
