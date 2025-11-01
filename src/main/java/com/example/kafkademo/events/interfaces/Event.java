package com.example.kafkademo.events.interfaces;

import com.example.kafkademo.enums.EventType;

import java.util.Map;

public interface Event {
    String getEventId();
    String getUserId();
    long getTimestamp();
    Map<String, String> getProperties();
    EventType getEventType();
}
