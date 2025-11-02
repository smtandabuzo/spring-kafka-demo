package com.example.kafkademo.events;

import com.example.kafkademo.enums.EventType;
import com.example.kafkademo.events.interfaces.EcommerceEvent;
import com.example.kafkademo.product.ItemContext;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class AddToCartEvent extends BaseEvent implements EcommerceEvent {
    private final ItemContext item;
    private String orderId;
    private BigDecimal value;
    private String currency;

    @Builder
    public AddToCartEvent(String userId, ItemContext item, int quantity) {
        super(userId);
        this.item = item;
        this.item.setQuantity(quantity);
        this.value = item.getPrice().multiply(BigDecimal.valueOf(quantity));
        this.currency = item.getCurrency();
    }

    @Override
    public EventType getEventType() {
        return EventType.ADD_TO_CART;
    }
}
