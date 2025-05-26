package com.jj.market.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.jj.market.dto.ChatMessageDTO;
import com.jj.market.entity.*;
import com.jj.market.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jj.market.repository.ChatRoomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserService userService;
    private final ProductService productService;

    @Transactional
    public String createChatRoom(Long productId, String sellerId, String buyerId) {
        User seller = userService.findByUserID(sellerId);
        User buyer = userService.findByUserID(buyerId);
        Product product = productService.getProductById(productId);


        Optional<ChatRoom> existingRoom = chatRoomRepository
            .findByProductAndBuyerAndSeller(product, buyer, seller);

        if (existingRoom.isPresent()) {
            return existingRoom.get().getRoomId();
        }


        ChatRoom chatRoom = ChatRoom.builder()
                .roomId(UUID.randomUUID().toString())
                .seller(seller)
                .buyer(buyer)
                .product(product)
                .createdAt(LocalDateTime.now())
                .status(ChatRoomStatus.ACTIVE)
                .buyerUnreadCount(0)
                .sellerUnreadCount(0)
                .build();

        chatRoomRepository.save(chatRoom);
        return chatRoom.getRoomId();
    }

    @Transactional(readOnly = true)
    public List<ChatRoom> getChatRooms(String userId) {
        User user = userService.findByUserID(userId);
        return chatRoomRepository.findByBuyerOrSellerAndStatus(user, user, ChatRoomStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public ChatRoom getChatRoom(String roomId) {
        return chatRoomRepository.findByRoomIdWithUsers(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));
    }

    @Transactional
    public void saveMessage(ChatMessageDTO chatMessage) {
        ChatRoom chatRoom = getChatRoom(chatMessage.getRoomId());
        User sender = userService.findByUserID(chatMessage.getSender());

        ChatMessage message = ChatMessage.builder()
                .type(chatMessage.getType())
                .chatRoom(chatRoom)
                .sender(sender)
                .message(chatMessage.getMessage())
                .timestamp(chatMessage.getTimestamp())
                .isRead(false)
                .build();

        chatMessageRepository.save(message);

        // 마지막 메시지 시간 업데이트
        chatRoom.setLastMessageTime(chatMessage.getTimestamp());
        chatRoomRepository.save(chatRoom);
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getChatMessages(String roomId) {
        ChatRoom chatRoom = getChatRoom(roomId);
        return chatMessageRepository.findByChatRoomOrderByTimestampAsc(chatRoom);
    }

    @Transactional
    public void leaveRoom(String roomId, String userId) {
        ChatRoom chatRoom = getChatRoom(roomId);
        chatRoom.setStatus(ChatRoomStatus.INACTIVE);
        chatRoomRepository.save(chatRoom);
    }
}
