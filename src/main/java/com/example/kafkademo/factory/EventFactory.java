package com.example.kafkademo.factory;

import com.example.kafkademo.events.AddToCartEvent;
import com.example.kafkademo.events.PageViewEvent;
import com.example.kafkademo.events.PurchaseEvent;
import com.example.kafkademo.events.interfaces.Event;
import com.example.kafkademo.product.ItemContext;
import com.example.kafkademo.user.UserContext;

import java.util.List;

public class EventFactory {
    public static Event createPageView(UserContext userContext, String pageUrl, String pageTitle) {
        return PageViewEvent.builder()
                .userId(userContext.getUserId())
                .pageUrl(pageUrl)
                .pageTitle(pageTitle)
                .build();
    }

    public static Event createAddToCart(String userId, ItemContext item, int quantity) {
        return AddToCartEvent.builder()
                .userId(userId)
                .item(item)
                .quantity(quantity)
                .build();
    }

    public static Event createPurchase(String userId, String orderId,
                                       List<ItemContext> items,
                                       String paymentMethod) {
        return PurchaseEvent.builder()
                .userId(userId)
                .orderId(orderId)
                .items(items)
                .paymentMethod(paymentMethod)
                .currency(items.get(0).getCurrency())
                .build();
    }
}
