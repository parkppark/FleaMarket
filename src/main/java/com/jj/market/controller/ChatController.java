package com.jj.market.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.jj.market.dto.ChatRoomRequest;
import com.jj.market.entity.ChatRoom;
import com.jj.market.service.ChatService;
import org.springframework.security.core.Authentication;
import lombok.RequiredArgsConstructor;
import java.util.Collections;
import java.util.List;
import com.jj.market.dto.ChatMessageDTO;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ChatController {
    private final ChatService chatService;

    @MessageMapping("/chat/send")
    @SendTo("/topic/chat/{roomId}")
    @PreAuthorize("isAuthenticated()")
    public ChatMessageDTO sendMessage(@Payload ChatMessageDTO chatMessage) {
        chatMessage.setTimestamp(LocalDateTime.now());
        return chatMessage;
    }

    @GetMapping("/chat/{roomId}")
    @PreAuthorize("isAuthenticated()")
    public String chatRoom(@PathVariable String roomId, Model model) {
        model.addAttribute("roomId", roomId);
        return "chat";
    }

    @PostMapping("/chat/room")
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    public Map<String, String> createRoom(@RequestBody ChatRoomRequest request,
                                          Authentication authentication) {
        String roomId = chatService.createChatRoom(
                request.getProductId(),
                request.getSellerId(),
                authentication.getName()
        );
        return Collections.singletonMap("roomId", roomId);
    }

    @GetMapping("/chat/rooms")
    @PreAuthorize("isAuthenticated()")
    public String chatRooms(Model model, Authentication authentication) {
        List<ChatRoom> chatRooms = chatService.getChatRooms(authentication.getName());
        model.addAttribute("chatRooms", chatRooms);
        return "chatRooms";
    }
}