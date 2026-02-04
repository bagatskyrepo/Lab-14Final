package com.example.demo.repository;

import com.example.demo.model.RefreshToken;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    // find token by token string
    Optional<RefreshToken> findByToken(String token);

    // for deleting tokens by user if user logs out
    @Modifying
    void deleteByUser(User user);
}