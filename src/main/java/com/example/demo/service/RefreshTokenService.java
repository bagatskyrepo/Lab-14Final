package com.example.demo.service;

import com.example.demo.model.RefreshToken;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration}")
    private Long refreshDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    // create new refresh token for user
    public RefreshToken createRefreshToken(String email) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(userRepository.findByEmail(email).get());
        // expiry date = now + duration from application.properties
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshDurationMs));
        // random UUID string as token
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }

    // check if the token is expired
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    // delete refresh tokens by user id
    @Transactional
    public void deleteByUserId(Integer userId) {
        // find user by id
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        refreshTokenRepository.deleteByUser(user);
    }
}