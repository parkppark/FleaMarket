package com.jj.market.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jj.market.entity.ChatRoom;
import com.jj.market.entity.ChatRoomStatus;
import com.jj.market.entity.Product;
import com.jj.market.entity.User;
import com.jj.market.repository.ChatRoomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {
    
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
        return chatRoomRepository.findByBuyerOrSeller(user, user);
    }
}