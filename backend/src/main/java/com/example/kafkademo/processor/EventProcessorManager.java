package com.example.kafkademo.processor;

import com.example.kafkademo.events.interfaces.Event;
import com.example.kafkademo.events.interfaces.EventProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventProcessorManager {
    private final List<EventProcessor> processors;

    public void processEvent(Event event) {
        processors.stream()
                .filter(p -> p.canProcess(event))
                .forEach(p -> p.process(event));
    }
}
