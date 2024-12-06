package com.kalado.authentication.application.service;

import com.kalado.common.dto.AuthDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.enums.Role;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.user.UserApi;
import com.kalado.common.response.LoginResponse;
import com.kalado.authentication.domain.model.AuthenticationInfo;
import com.kalado.authentication.infrastructure.repository.AuthenticationRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

  private final AuthenticationRepository authRepository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final RedisTemplate<String, Long> redisTemplate;
  private final UserApi userApi;

  private static final String SECRET_KEY = "asdffffffg3Md4Esdafdasff322432PHdtxFzd23543rsafYdnQVNt1mGPJwfdsgsfdgsdflSVLY45csfgsGRyMwmN4";

  private static final long TOKEN_EXPIRATION_TIME = 24 * 60 * 60 * 1000;

  public LoginResponse login(String username, String password) {
    validateLoginInput(username, password);

    AuthenticationInfo authInfo = authRepository.findByUsername(username);
    if (authInfo == null || !passwordEncoder.matches(password, authInfo.getPassword())) {
      log.warn("Invalid login attempt for username: {}", username);
      throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Invalid username or password");
    }

    String token = generateToken(authInfo.getUserId());
    return LoginResponse.builder().token(token).role(authInfo.getRole()).build();
  }

  public AuthDto validateToken(String tokenValue) {
    Optional<Claims> claims = extractAllClaims(tokenValue);

    if (claims.isEmpty()) {
      return AuthDto.builder().isValid(false).build();
    }

    String userId = claims.get().getSubject();
    if (!isTokenValid(tokenValue, userId, claims.get().getExpiration())) {
      return AuthDto.builder().isValid(false).build();
    }

    return authRepository
        .findById(Long.valueOf(userId))
        .map(
            authInfo ->
                AuthDto.builder()
                    .isValid(true)
                    .userId(authInfo.getUserId())
                    .role(authInfo.getRole())
                    .build())
        .orElseGet(() -> AuthDto.builder().isValid(false).build());
  }

  public void invalidateToken(String token) {
    if (Boolean.TRUE.equals(redisTemplate.hasKey(token))) {
      redisTemplate.delete(token);
      log.info("Token invalidated: {}", token);
    }
  }

  private String generateToken(long userId) {
    long nowMillis = System.currentTimeMillis();
    long expMillis = nowMillis + TOKEN_EXPIRATION_TIME;

    String token = generateTokenValue(userId, expMillis, nowMillis);
    redisTemplate.opsForValue().set(token, userId, TOKEN_EXPIRATION_TIME, TimeUnit.MILLISECONDS);
    return token;
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

  private Optional<Claims> extractAllClaims(String token) {
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

  private boolean isTokenValid(String token, String userId, Date expiration) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(token))
        && Objects.nonNull(userId)
        && new Date().before(expiration);
  }

  public void register(String username, String password, Role role) {
    validateRegistrationInput(username, password, role);

    AuthenticationInfo existingUser = authRepository.findByUsername(username);
    if (Objects.nonNull(existingUser)) {
      log.info("User already exists: {}", existingUser);
      throw new CustomException(ErrorCode.USER_ALREADY_EXISTS, "User already exists");
    }

    String encodedPassword = passwordEncoder.encode(password);

    AuthenticationInfo authenticationInfo =
        authRepository.save(
            AuthenticationInfo.builder()
                .username(username)
                .password(encodedPassword)
                .role(role)
                .build());

    assignUserRole(authenticationInfo, role);
  }

  private void assignUserRole(AuthenticationInfo authenticationInfo, Role role) {
    switch (role) {
      case ADMIN ->
          userApi.createAdmin(AdminDto.builder().id(authenticationInfo.getUserId()).build());
      case USER ->
          userApi.createUser(
              UserDto.builder().id(authenticationInfo.getUserId()).build());
    }
  }

  private void validateLoginInput(String username, String password) {
    if (username == null || username.isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Username cannot be empty");
    }
    if (password == null || password.isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Password cannot be empty");
    }
  }

  private void validateRegistrationInput(String username, String password, Role role) {
    if (username == null || username.isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Username cannot be empty");
    }
    if (password == null || password.isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Password cannot be empty");
    }
    if (role == null) {
      throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Role cannot be null");
    }
  }

  public String getUsername(Long userId) {
    return authRepository.findById(userId).map(AuthenticationInfo::getUsername).orElse(null);
  }
}
