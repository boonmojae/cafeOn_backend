package com.b1a4.cafeOn.chat.repository;

public interface LastReadView {
    String getUserId();
    Long getLastReadChatId();
}
