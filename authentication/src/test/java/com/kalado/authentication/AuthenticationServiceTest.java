package com.kalado.authentication;

import com.kalado.common.dto.AuthDto;
import com.kalado.common.dto.UserDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.enums.Role;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.authentication.AuthenticationApi;
import com.kalado.common.feign.user.UserApi;
import com.kalado.common.response.LoginResponse;
import com.kalado.authentication.application.service.*;
import com.kalado.authentication.domain.model.AuthenticationInfo;
import com.kalado.authentication.infrastructure.repository.AuthenticationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationRepository authRepository;

    @Mock
    private UserApi userApi;

    @Mock
    private VerificationService verificationService;

    @Mock
    private TokenService tokenService;

    @Mock
    private PasswordResetService passwordResetService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private AuthenticationInfo testUser;
    private static final String TEST_USERNAME = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        testUser = AuthenticationInfo.builder()
                .userId(TEST_USER_ID)
                .username(TEST_USERNAME)
                .password("encoded_password")
                .role(Role.USER)
                .build();
    }

    @Test
    void login_Success() {
        // Arrange
        when(authRepository.findByUsername(TEST_USERNAME)).thenReturn(testUser);
        when(passwordResetService.verifyPassword(testUser, TEST_PASSWORD)).thenReturn(true);
        when(verificationService.isEmailVerified(testUser)).thenReturn(true);
        when(userApi.getUserProfile(TEST_USER_ID))
                .thenReturn(UserDto.builder().blocked(false).build());
        when(tokenService.generateToken(TEST_USER_ID)).thenReturn("generated.token");

        // Act
        LoginResponse response = authenticationService.login(TEST_USERNAME, TEST_PASSWORD);

        // Assert
        assertNotNull(response);
        assertEquals("generated.token", response.getToken());
        assertEquals(Role.USER, response.getRole());
        verify(verificationService).isEmailVerified(testUser);
        verify(tokenService).generateToken(TEST_USER_ID);
    }

    @Test
    void login_InvalidCredentials() {
        // Arrange
        when(authRepository.findByUsername(TEST_USERNAME)).thenReturn(testUser);
        when(passwordResetService.verifyPassword(testUser, TEST_PASSWORD)).thenReturn(false);

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class,
                () -> authenticationService.login(TEST_USERNAME, TEST_PASSWORD));

        assertEquals(ErrorCode.INVALID_CREDENTIALS, exception.getErrorCode());
        verify(tokenService, never()).generateToken(anyLong());
    }

    @Test
    void login_EmailNotVerified() {
        // Arrange
        when(authRepository.findByUsername(TEST_USERNAME)).thenReturn(testUser);
        when(passwordResetService.verifyPassword(testUser, TEST_PASSWORD)).thenReturn(true);
        when(verificationService.isEmailVerified(testUser)).thenReturn(false);

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class,
                () -> authenticationService.login(TEST_USERNAME, TEST_PASSWORD));

        assertEquals(ErrorCode.EMAIL_NOT_VERIFIED, exception.getErrorCode());
        verify(tokenService, never()).generateToken(anyLong());
    }

    @Test
    void login_BlockedUser() {
        when(authRepository.findByUsername(TEST_USERNAME)).thenReturn(testUser);
        when(passwordResetService.verifyPassword(testUser, TEST_PASSWORD)).thenReturn(true);
        when(verificationService.isEmailVerified(testUser)).thenReturn(true);
        when(userApi.getUserProfile(TEST_USER_ID))
                .thenReturn(UserDto.builder().blocked(true).build());

        CustomException exception = assertThrows(CustomException.class,
                () -> authenticationService.login(TEST_USERNAME, TEST_PASSWORD));

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
        verify(tokenService, never()).generateToken(anyLong());
        verify(authRepository).findByUsername(TEST_USERNAME);
    }

    @Test
    void validateToken_Success() {
        // Arrange
        String token = "valid.token";
        AuthDto expectedAuthDto = AuthDto.builder()
                .isValid(true)
                .userId(TEST_USER_ID)
                .role(Role.USER)
                .build();
        when(tokenService.validateTokenAndCreateAuthDto(token, authRepository))
                .thenReturn(expectedAuthDto);

        // Act
        AuthDto result = authenticationService.validateToken(token);

        // Assert
        assertTrue(result.isValid());
        assertEquals(TEST_USER_ID, result.getUserId());
        assertEquals(Role.USER, result.getRole());
    }

    @Test
    void invalidateToken_Success() {
        // Arrange
        String token = "token.to.invalidate";

        // Act
        authenticationService.invalidateToken(token);

        // Assert
        verify(tokenService).invalidateToken(token);
    }

    @Test
    void updateUserRole_Success() {
        // Arrange
        Long targetUserId = 2L;
        Role newRole = Role.ADMIN;
        Long requestingUserId = 3L;

        // Act
        authenticationService.updateUserRole(targetUserId, newRole, requestingUserId);

        // Assert
        verify(roleService).updateUserRole(targetUserId, newRole, requestingUserId);
    }

    @Test
    void findUserById_Success() {
        // Arrange
        when(authRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // Act
        Optional<AuthenticationInfo> result = authenticationService.findUserById(TEST_USER_ID);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(TEST_USER_ID, result.get().getUserId());
        assertEquals(TEST_USERNAME, result.get().getUsername());
    }

    @Test
    void getUsername_Success() {
        // Arrange
        when(authRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // Act
        String username = authenticationService.getUsername(TEST_USER_ID);

        // Assert
        assertEquals(TEST_USERNAME, username);
    }

    @Test
    void getUsername_UserNotFound() {
        // Arrange
        when(authRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // Act
        String username = authenticationService.getUsername(TEST_USER_ID);

        // Assert
        assertNull(username);
    }
}