package com.jj.market.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.jj.market.dto.ChatRoomRequest;
import com.jj.market.dto.ChatMessageDTO;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.handler.annotation.Header;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chat")
@PreAuthorize("isAuthenticated()")
public class ChatController {
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // 채팅 메시지 전송
    @MessageMapping("/chat/send")
    @PreAuthorize("isAuthenticated()")
    public void sendMessage(@Payload ChatMessageDTO chatMessage, 
                          @Header("simpSessionId") String sessionId,
                          Principal principal) {
        if (principal == null) {
            // 인증되지 않은 사용자의 메시지는 처리하지 않음
            return;
        }

        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessage.setSender(principal.getName());

        ChatRoom chatRoom = chatService.getChatRoom(chatMessage.getRoomId());
        String recipientId = chatRoom.getSeller().getUserID().equals(principal.getName()) 
            ? chatRoom.getBuyer().getUserID() 
            : chatRoom.getSeller().getUserID();

        // 수신자에게만 메시지 전송
        messagingTemplate.convertAndSendToUser(
            recipientId,
            "/queue/messages",
            chatMessage
        );

        // 메시지 저장
        chatService.saveMessage(chatMessage);
    }

    // 채팅방 목록 페이지
    @GetMapping("/rooms")
    public String chatRooms(Model model, Authentication authentication) {
        List<ChatRoom> chatRooms = chatService.getChatRooms(authentication.getName());
        model.addAttribute("chatRooms", chatRooms);
        return "chatRooms";
    }

    // 특정 채팅방 입장
    @GetMapping("/{roomId}")
    public String chatRoom(@PathVariable String roomId, Model model, Authentication authentication) {
        ChatRoom chatRoom = chatService.getChatRoom(roomId);

        // 현재 사용자가 해당 채팅방의 참여자인지 확인
        if (!chatRoom.getBuyer().getUserID().equals(authentication.getName()) && 
            !chatRoom.getSeller().getUserID().equals(authentication.getName())) {
            return "redirect:/chat/rooms";
        }

        // 채팅방 정보와 이전 메시지들을 모델에 추가
        model.addAttribute("roomId", roomId);
        model.addAttribute("chatRoom", chatRoom);
        model.addAttribute("messages", chatService.getChatMessages(roomId));
        return "chat";
    }

    // 새로운 채팅방 생성
    @PostMapping("/room")
    @ResponseBody
    public Map<String, String> createRoom(@RequestBody ChatRoomRequest request,
                                        Authentication authentication) {
        String roomId = chatService.createChatRoom(
                request.getProductId(),
                request.getSellerId(),
                authentication.getName()
        );
        return Collections.singletonMap("roomId", roomId);
    }

    // 채팅방 나가기
    @PostMapping("/{roomId}/leave")
    @ResponseBody
    public Map<String, String> leaveRoom(@PathVariable String roomId, 
                                       Authentication authentication) {
        chatService.leaveRoom(roomId, authentication.getName());
        return Collections.singletonMap("status", "success");
    }
}
