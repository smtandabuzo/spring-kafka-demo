package com.example.kafkademo.user;

import lombok.Builder;
import lombok.Data;

// UserContext.java
@Data
@Builder
public class UserContext {
    private String userId;
    private String sessionId;
    private String userAgent;
    private String ipAddress;
    private String country;
    private String city;
    private String deviceType;
    private String os;
    private String browser;
}
