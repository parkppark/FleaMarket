package com.jj.market.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jj.market.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

	User findByUserID(String userID);
	
	Optional<User> findByEmail(String providerEmail);

	User findByProviderId(String providerId);
}