package com.kalado.authentication;

import com.kalado.authentication.application.service.AuthenticationService;
import com.kalado.authentication.application.service.VerificationService;
import com.kalado.authentication.domain.model.AuthenticationInfo;
import com.kalado.authentication.infrastructure.repository.AuthenticationRepository;
import com.kalado.common.dto.AuthDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.enums.Role;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.user.UserApi;
import com.kalado.common.response.LoginResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    private static final String SECRET_KEY = "X71wHJEhg1LQE5DzWcdc/BRAgIvnqHYiZHBbqgrBOZLzwlHlHh/W1ScQGwd1XM8V1c5vtgGlDS8lb64zjZEZXg==";

    @Mock
    private AuthenticationRepository authRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private RedisTemplate<String, Long> redisTemplate;

    @Mock
    private ValueOperations<String, Long> valueOperations;

    @Mock
    private UserApi userApi;

    @Mock
    private VerificationService verificationService;

    @InjectMocks
    private AuthenticationService authService;

    @Test
    void login_WithValidCredentials_ShouldSucceed() {
        String username = "testuser";
        String password = "testpass";
        String encodedPassword = "encodedpass";
        AuthenticationInfo user = AuthenticationInfo.builder()
                .userId(1L)
                .username(username)
                .password(encodedPassword)
                .role(Role.USER)
                .build();

        when(authRepository.findByUsername(username)).thenReturn(user);
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(verificationService.isEmailVerified(user)).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        LoginResponse response = authService.login(username, password);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals(Role.USER, response.getRole());
        verify(valueOperations).set(anyString(), eq(1L), anyLong(), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void login_WithUnverifiedEmail_ShouldThrowException() {
        String username = "testuser";
        String password = "testpass";
        String encodedPassword = "encodedpass";
        AuthenticationInfo user = AuthenticationInfo.builder()
                .username(username)
                .password(encodedPassword)
                .role(Role.USER)
                .build();

        when(authRepository.findByUsername(username)).thenReturn(user);
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(verificationService.isEmailVerified(user)).thenReturn(false);

        CustomException exception = assertThrows(
                CustomException.class,
                () -> authService.login(username, password)
        );
        assertEquals(ErrorCode.EMAIL_NOT_VERIFIED, exception.getErrorCode());
    }

    @Test
    void register_WithNewUser_ShouldSucceed() {
        String username = "newuser";
        String password = "password";
        String encodedPassword = "encodedpass";
        AuthenticationInfo savedUser = AuthenticationInfo.builder()
                .userId(1L)
                .username(username)
                .password(encodedPassword)
                .role(Role.USER)
                .build();

        when(authRepository.findByUsername(username)).thenReturn(null);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(authRepository.save(any(AuthenticationInfo.class))).thenReturn(savedUser);

        authService.register(username, password, Role.USER);

        verify(authRepository).save(any(AuthenticationInfo.class));
        verify(userApi).createUser(any());
        verify(verificationService).createVerificationToken(any());
    }

    @Test
    void register_WithExistingUsername_ShouldThrowException() {
        String username = "existinguser";
        String password = "password";
        AuthenticationInfo existingUser = AuthenticationInfo.builder()
                .username(username)
                .password("encodedpass")
                .role(Role.USER)
                .build();

        when(authRepository.findByUsername(username)).thenReturn(existingUser);

        CustomException exception = assertThrows(
                CustomException.class,
                () -> authService.register(username, password, Role.USER)
        );
        assertEquals(ErrorCode.USER_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnValidAuthDto() {
        String validToken = generateValidToken(1L);
        AuthenticationInfo user = AuthenticationInfo.builder()
                .userId(1L)
                .username("testuser")
                .role(Role.USER)
                .build();

        when(redisTemplate.hasKey(validToken)).thenReturn(true);
        when(authRepository.findById(1L)).thenReturn(Optional.of(user));

        AuthDto authDto = authService.validateToken(validToken);

        assertTrue(authDto.isValid());
        assertEquals(user.getUserId(), authDto.getUserId());
        assertEquals(user.getRole(), authDto.getRole());
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnInvalidAuthDto() {
        String invalidToken = "invalid.token.here";
        AuthDto authDto = authService.validateToken(invalidToken);
        assertFalse(authDto.isValid());
    }

    @Test
    void invalidateToken_ShouldRemoveFromRedis() {
        String token = "valid.token.here";
        when(redisTemplate.hasKey(token)).thenReturn(true);

        authService.invalidateToken(token);

        verify(redisTemplate).delete(token);
    }

    @Test
    void getUsername_WithValidUserId_ShouldReturnUsername() {
        AuthenticationInfo user = AuthenticationInfo.builder()
                .userId(1L)
                .username("testuser")
                .build();

        when(authRepository.findById(1L)).thenReturn(Optional.of(user));

        String username = authService.getUsername(1L);

        assertEquals("testuser", username);
    }

    private String generateValidToken(long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new java.util.Date(System.currentTimeMillis()))
                .setExpiration(new java.util.Date(System.currentTimeMillis() + 3600000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY)), io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();
    }
}