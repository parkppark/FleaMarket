package com.jj.market.entity;

import lombok.Getter;

@Getter
public enum ChatRoomStatus {
    ACTIVE("활성"),
    DELETED("삭제됨");
    
    private final String description;
    
    ChatRoomStatus(String description) {
        this.description = description;
    }
}