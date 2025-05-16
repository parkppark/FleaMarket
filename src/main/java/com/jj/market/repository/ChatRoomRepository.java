package com.jj.market.repository;

import com.jj.market.entity.ChatRoom;
import com.jj.market.entity.Product;
import com.jj.market.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByProductAndBuyerAndSeller(
            Product product,
            User buyer,
            User seller
    );


    List<ChatRoom> findByBuyerOrSeller(
            User buyer,
            User seller
    );


    List<ChatRoom> findByProduct(Product product);


    List<ChatRoom> findAllByOrderByCreatedAtDesc();
}