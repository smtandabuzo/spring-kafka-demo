package com.example.kafkademo.enums;

public enum EventType {
    // User Actions
    PAGE_VIEW,
    SEARCH,
    CLICK,

    // E-commerce
    ADD_TO_CART,
    REMOVE_FROM_CART,
    VIEW_ITEM,
    VIEW_CART,
    INITIATE_CHECKOUT,
    ADD_PAYMENT_INFO,
    PURCHASE,
    ADD_TO_WISHLIST,

    // User Account
    SIGN_UP,
    LOGIN,
    LOGOUT,
    PASSWORD_RESET,

    // Engagement
    RATE,
    REVIEW,
    SHARE,
    SUBSCRIBE,

    // System
    ERROR,
    SESSION_START,
    SESSION_END
}
