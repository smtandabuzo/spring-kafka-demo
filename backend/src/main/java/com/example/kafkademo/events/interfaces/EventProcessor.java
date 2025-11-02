package com.example.kafkademo.events.interfaces;

public interface EventProcessor {
    boolean canProcess(Event event);
    void process(Event event);
}
