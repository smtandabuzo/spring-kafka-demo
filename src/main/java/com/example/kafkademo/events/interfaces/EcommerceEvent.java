package com.example.kafkademo.events.interfaces;

public interface EcommerceEvent extends Event {
    String getOrderId();
    double getValue();
    String getCurrency();
}
