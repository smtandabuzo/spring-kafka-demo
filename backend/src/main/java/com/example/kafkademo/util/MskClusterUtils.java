package com.example.kafkademo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.Properties;

@Component
public class MskClusterUtils {
    private static final Logger log = LoggerFactory.getLogger(MskClusterUtils.class);

    @Autowired
    private KafkaAdmin kafkaAdmin;

    @PostConstruct
    public void init() {
        log.info("Kafka Admin Client configured with bootstrap servers: {}", 
                kafkaAdmin.getConfigurationProperties().get("bootstrap.servers"));
        log.info("MSK cluster connection will be established on first use");
    }

    public String getBootstrapServers() {
        return (String) kafkaAdmin.getConfigurationProperties().get("bootstrap.servers");
    }
}
