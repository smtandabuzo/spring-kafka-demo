package com.example.kafkademo.events.interfaces;

import java.math.BigDecimal;

public interface EcommerceEvent extends Event {
    String getOrderId();
    BigDecimal getValue();
    String getCurrency();
}
