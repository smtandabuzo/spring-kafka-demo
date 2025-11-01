package com.example.kafkademo.events;

import com.example.kafkademo.enums.EventType;
import com.example.kafkademo.events.interfaces.EcommerceEvent;
import com.example.kafkademo.product.ItemContext;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseEvent extends BaseEvent implements EcommerceEvent {
    private String orderId;
    private double value;
    private String currency;
    private String paymentMethod;
    private List<ItemContext> items;

    @Builder
    public PurchaseEvent(String userId, String orderId, List<ItemContext> items,
                         String paymentMethod, String currency) {
        super(userId);
        this.orderId = orderId;
        this.items = items;
        this.paymentMethod = paymentMethod;
        this.currency = currency;
        this.value = items.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
    }

    @Override
    public EventType getEventType() {
        return EventType.PURCHASE;
    }
}
