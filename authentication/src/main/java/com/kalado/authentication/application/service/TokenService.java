package com.kalado.authentication.application.service;

import com.kalado.authentication.domain.model.AuthenticationInfo;
import com.kalado.authentication.infrastructure.repository.AuthenticationRepository;
import com.kalado.common.dto.AuthDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {
    private final RedisTemplate<String, Long> redisTemplate;

    private static final String SECRET_KEY = "X71wHJEhg1LQE5DzWcdc/BRAgIvnqHYiZHBbqgrBOZLzwlHlHh/W1ScQGwd1XM8V1c5vtgGlDS8lb64zjZEZXg==";
    private static final long TOKEN_EXPIRATION_TIME = 24 * 60 * 60 * 1000;  // 24 hours

    public String generateToken(long userId) {
        long nowMillis = System.currentTimeMillis();
        long expMillis = nowMillis + TOKEN_EXPIRATION_TIME;

        String token = generateTokenValue(userId, expMillis, nowMillis);
        storeTokenInRedis(token, userId);
        return token;
    }

    private void storeTokenInRedis(String token, long userId) {
        redisTemplate.opsForValue().set(token, userId, TOKEN_EXPIRATION_TIME, TimeUnit.MILLISECONDS);
    }

    private String generateTokenValue(long userId, long expMillis, long nowMillis) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date(nowMillis))
                .setExpiration(new Date(expMillis))
                .setId(UUID.randomUUID().toString())
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Optional<Claims> extractClaims(String token) {
        try {
            return Optional.ofNullable(
                    Jwts.parserBuilder()
                            .setSigningKey(getSignInKey())
                            .build()
                            .parseClaimsJws(token)
                            .getBody());
        } catch (Exception e) {
            log.error("Failed to parse token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public boolean isTokenValid(String token, String userId, Date expiration) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(token))
                && Objects.nonNull(userId)
                && new Date().before(expiration);
    }

    public void invalidateToken(String token) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(token))) {
            redisTemplate.delete(token);
            log.info("Token invalidated: {}", token);
        }
    }

    public AuthDto validateTokenAndCreateAuthDto(String tokenValue, AuthenticationRepository authRepository) {
        Optional<Claims> claims = extractClaims(tokenValue);

        if (claims.isEmpty()) {
            return AuthDto.builder().isValid(false).build();
        }

        String userId = claims.get().getSubject();
        if (!isTokenValid(tokenValue, userId, claims.get().getExpiration())) {
            return AuthDto.builder().isValid(false).build();
        }

        return authRepository
                .findById(Long.valueOf(userId))
                .map(authInfo -> AuthDto.builder()
                        .isValid(true)
                        .userId(authInfo.getUserId())
                        .role(authInfo.getRole())
                        .build())
                .orElseGet(() -> AuthDto.builder().isValid(false).build());
    }

    public void invalidateUserTokens(Long userId) {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null) {
            keys.stream()
                    .filter(key -> {
                        Long storedUserId = redisTemplate.opsForValue().get(key);
                        return storedUserId != null && storedUserId.equals(userId);
                    })
                    .forEach(this::invalidateToken);
            log.info("Invalidated all tokens for user ID: {}", userId);
        }
    }
}