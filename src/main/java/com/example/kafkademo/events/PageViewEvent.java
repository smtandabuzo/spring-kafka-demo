package com.example.kafkademo.events;

import com.example.kafkademo.enums.EventType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PageViewEvent extends BaseEvent {
    private String pageUrl;
    private String pageTitle;
    private String referrer;

    @Builder
    public PageViewEvent(String userId, String pageUrl, String pageTitle, String referrer) {
        super(userId);
        this.pageUrl = pageUrl;
        this.pageTitle = pageTitle;
        this.referrer = referrer;
        addProperty("url", pageUrl);
        if (pageTitle != null) addProperty("pageTitle", pageTitle);
        if (referrer != null) addProperty("referrer", referrer);
    }

    @Override
    public EventType getEventType() {
        return EventType.PAGE_VIEW;
    }
}
