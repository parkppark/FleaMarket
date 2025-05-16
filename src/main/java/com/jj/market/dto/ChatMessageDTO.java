package com.jj.market.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class ChatMessageDTO {
    private String type;
    private String roomId;
    private String sender;
    private String message;
    private LocalDateTime timestamp;
}