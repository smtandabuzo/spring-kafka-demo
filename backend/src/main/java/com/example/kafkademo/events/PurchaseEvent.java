package com.example.kafkademo.events;

import com.example.kafkademo.enums.EventType;
import com.example.kafkademo.events.interfaces.EcommerceEvent;
import com.example.kafkademo.product.ItemContext;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class PurchaseEvent extends BaseEvent implements EcommerceEvent {
    private String orderId;
    private BigDecimal value;
    private String paymentMethod;
    private String currency;
    private List<ItemContext> items;
    
    // This field is already in BaseEvent, no need to redeclare it
    // private String userId;
    @Override
    public BigDecimal getValue() {
        return this.value;
    }
    
    @Override
    public String getOrderId() {
        return this.orderId;
    }
    
    @Override
    public String getCurrency() {
        return this.currency;
    }

    public PurchaseEvent() {
        super();
    }
    
    public PurchaseEvent(String userId, String orderId, List<ItemContext> items, 
                        String paymentMethod, String currency, BigDecimal value) {
        super(userId);
        this.orderId = orderId;
        this.items = items != null ? items : List.of();
        this.paymentMethod = paymentMethod;
        this.currency = currency;
        this.currency = currency;
        this.value = value;
    }
    
    @JsonCreator
    public static PurchaseEvent create(
            @JsonProperty("userId") String userId,
            @JsonProperty("orderId") String orderId,
            @JsonProperty("items") List<ItemContext> items,
            @JsonProperty("paymentMethod") String paymentMethod,
            @JsonProperty("currency") String currency,
            @JsonProperty("value") BigDecimal value) {
        
        // If value is 0 and we have items, calculate the total from items
        if (value.compareTo(BigDecimal.ZERO) == 0 && items != null && !items.isEmpty()) {
            value = items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        
        PurchaseEvent event = new PurchaseEvent(userId, orderId, items, paymentMethod, currency, value);
        event.setUserId(userId);  // Ensure userId is set in the base class
        return event;
    }
    
    @Override
    public EventType getEventType() {
        return EventType.PURCHASE;
    }
}
