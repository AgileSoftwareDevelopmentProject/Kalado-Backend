package com.kalado.authentication;

import com.kalado.authentication.application.service.RoleService;
import com.kalado.authentication.configuration.AdminConfiguration;
import com.kalado.authentication.domain.model.AuthenticationInfo;
import com.kalado.authentication.infrastructure.repository.AuthenticationRepository;
import com.kalado.common.dto.AdminDto;
import com.kalado.common.dto.UserDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.enums.Role;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.user.UserApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private AuthenticationRepository authRepository;

    @Mock
    private UserApi userApi;

    @Mock
    private AdminConfiguration adminConfig;

    @InjectMocks
    private RoleService roleService;

    @Captor
    private ArgumentCaptor<AuthenticationInfo> authInfoCaptor;

    @Captor
    private ArgumentCaptor<AdminDto> adminDtoCaptor;

    private AuthenticationInfo godUser;
    private AuthenticationInfo adminUser;
    private AuthenticationInfo regularUser;
    private UserDto userProfile;

    @BeforeEach
    void setUp() {
        godUser = AuthenticationInfo.builder()
                .userId(1L)
                .username("god@example.com")
                .role(Role.GOD)
                .build();

        adminUser = AuthenticationInfo.builder()
                .userId(2L)
                .username("admin@example.com")
                .role(Role.ADMIN)
                .build();

        regularUser = AuthenticationInfo.builder()
                .userId(3L)
                .username("user@example.com")
                .role(Role.USER)
                .build();

        userProfile = UserDto.builder()
                .id(3L)
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("1234567890")
                .build();
    }

    @Nested
    @DisplayName("Role Update Tests")
    class RoleUpdateTests {
        @Test
        @DisplayName("GOD should be able to promote USER to ADMIN")
        void updateUserRole_PromoteToAdmin() {
            // Arrange
            when(authRepository.findById(godUser.getUserId())).thenReturn(Optional.of(godUser));
            when(authRepository.findById(regularUser.getUserId())).thenReturn(Optional.of(regularUser));
            when(authRepository.save(any(AuthenticationInfo.class))).thenReturn(regularUser);
            when(userApi.getUserProfile(regularUser.getUserId())).thenReturn(userProfile);

            // Act
            roleService.updateUserRole(regularUser.getUserId(), Role.ADMIN, godUser.getUserId());

            // Assert
            verify(authRepository).save(authInfoCaptor.capture());
            assertEquals(Role.ADMIN, authInfoCaptor.getValue().getRole());

            verify(userApi).createAdmin(adminDtoCaptor.capture());
            AdminDto capturedAdminDto = adminDtoCaptor.getValue();
            assertEquals(regularUser.getUserId(), capturedAdminDto.getId());
            assertEquals(userProfile.getFirstName(), capturedAdminDto.getFirstName());
            assertEquals(userProfile.getLastName(), capturedAdminDto.getLastName());
        }

        @Test
        @DisplayName("GOD should be able to demote ADMIN to USER")
        void updateUserRole_DemoteToUser() {
            // Arrange
            when(authRepository.findById(godUser.getUserId())).thenReturn(Optional.of(godUser));
            when(authRepository.findById(adminUser.getUserId())).thenReturn(Optional.of(adminUser));
            when(authRepository.save(any(AuthenticationInfo.class))).thenReturn(adminUser);

            // Act
            roleService.updateUserRole(adminUser.getUserId(), Role.USER, godUser.getUserId());

            // Assert
            verify(authRepository).save(authInfoCaptor.capture());
            assertEquals(Role.USER, authInfoCaptor.getValue().getRole());
            verify(userApi, never()).createAdmin(any());
        }

        @Test
        @DisplayName("Should fail when non-GOD attempts role change")
        void updateUserRole_NonGodAttempt() {
            // Arrange
            when(authRepository.findById(adminUser.getUserId())).thenReturn(Optional.of(adminUser));
            when(authRepository.findById(regularUser.getUserId())).thenReturn(Optional.of(regularUser));

            // Act & Assert
            CustomException exception = assertThrows(CustomException.class,
                    () -> roleService.updateUserRole(regularUser.getUserId(), Role.ADMIN, adminUser.getUserId()));

            assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
            verify(authRepository, never()).save(any());
            verify(userApi, never()).createAdmin(any());
        }

        @Test
        @DisplayName("Should fail when attempting to modify GOD role")
        void updateUserRole_ModifyGodRole() {
            // Arrange
            AuthenticationInfo godTarget = AuthenticationInfo.builder()
                    .userId(4L)
                    .username("othergod@example.com")
                    .role(Role.GOD)
                    .build();

            when(authRepository.findById(godUser.getUserId())).thenReturn(Optional.of(godUser));
            when(authRepository.findById(godTarget.getUserId())).thenReturn(Optional.of(godTarget));

            // Act & Assert
            CustomException exception = assertThrows(CustomException.class,
                    () -> roleService.updateUserRole(godTarget.getUserId(), Role.ADMIN, godUser.getUserId()));

            assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
            assertEquals("Cannot modify GOD role", exception.getMessage());
            verify(authRepository, never()).save(any());
            verify(userApi, never()).createAdmin(any());
        }

        @Test
        @DisplayName("Should fail with invalid role transition")
        void updateUserRole_InvalidTransition() {
            // Arrange
            when(authRepository.findById(godUser.getUserId())).thenReturn(Optional.of(godUser));
            when(authRepository.findById(regularUser.getUserId())).thenReturn(Optional.of(regularUser));

            // Act & Assert
            CustomException exception = assertThrows(CustomException.class,
                    () -> roleService.updateUserRole(regularUser.getUserId(), Role.GOD, godUser.getUserId()));

            assertEquals(ErrorCode.INVALID_ROLE_TRANSITION, exception.getErrorCode());
            verify(authRepository, never()).save(any());
            verify(userApi, never()).createAdmin(any());
        }
    }

    @Nested
    @DisplayName("Privileged Registration Tests")
    class PrivilegedRegistrationTests {
        private static final String TEST_EMAIL = "test@example.com";

        @Test
        @DisplayName("Should validate GOD registration successfully")
        void validateGodRegistration_Success() {
            // Arrange
            when(adminConfig.isEmailAuthorizedForGod(TEST_EMAIL)).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() ->
                    roleService.validatePrivilegedRegistration(TEST_EMAIL, Role.GOD)
            );
        }

        @Test
        @DisplayName("Should fail unauthorized GOD registration")
        void validateGodRegistration_Unauthorized() {
            // Arrange
            when(adminConfig.isEmailAuthorizedForGod(TEST_EMAIL)).thenReturn(false);

            // Act & Assert
            CustomException exception = assertThrows(CustomException.class,
                    () -> roleService.validatePrivilegedRegistration(TEST_EMAIL, Role.GOD));

            assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
            assertEquals("GOD registration is restricted to authorized emails only", exception.getMessage());
        }

        @Test
        @DisplayName("Should validate ADMIN registration successfully")
        void validateAdminRegistration_Success() {
            // Arrange
            when(adminConfig.isEmailAuthorizedForAdmin(TEST_EMAIL)).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() ->
                    roleService.validatePrivilegedRegistration(TEST_EMAIL, Role.ADMIN)
            );
        }

        @Test
        @DisplayName("Should fail unauthorized ADMIN registration")
        void validateAdminRegistration_Unauthorized() {
            // Arrange
            when(adminConfig.isEmailAuthorizedForAdmin(TEST_EMAIL)).thenReturn(false);

            // Act & Assert
            CustomException exception = assertThrows(CustomException.class,
                    () -> roleService.validatePrivilegedRegistration(TEST_EMAIL, Role.ADMIN));

            assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
            assertEquals("Admin registration is restricted to authorized emails only", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        @Test
        @DisplayName("Should handle requesting user not found")
        void updateUserRole_RequestingUserNotFound() {
            // Arrange
            when(authRepository.findById(godUser.getUserId())).thenReturn(Optional.empty());

            // Act & Assert
            CustomException exception = assertThrows(CustomException.class,
                    () -> roleService.updateUserRole(regularUser.getUserId(), Role.ADMIN, godUser.getUserId()));

            assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
            assertEquals("Requesting user not found", exception.getMessage());
        }

        @Test
        @DisplayName("Should handle target user not found")
        void updateUserRole_TargetUserNotFound() {
            // Arrange
            when(authRepository.findById(godUser.getUserId())).thenReturn(Optional.of(godUser));
            when(authRepository.findById(regularUser.getUserId())).thenReturn(Optional.empty());

            // Act & Assert
            CustomException exception = assertThrows(CustomException.class,
                    () -> roleService.updateUserRole(regularUser.getUserId(), Role.ADMIN, godUser.getUserId()));

            assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
            assertEquals("Target user not found", exception.getMessage());
        }

        @Test
        @DisplayName("Should handle error during admin promotion")
        void updateUserRole_AdminPromotionError() {
            // Arrange
            when(authRepository.findById(godUser.getUserId())).thenReturn(Optional.of(godUser));
            when(authRepository.findById(regularUser.getUserId())).thenReturn(Optional.of(regularUser));
            when(authRepository.save(any(AuthenticationInfo.class))).thenReturn(regularUser);
            when(userApi.getUserProfile(regularUser.getUserId())).thenThrow(new RuntimeException("API Error"));

            // Act & Assert
            assertThrows(RuntimeException.class,
                    () -> roleService.updateUserRole(regularUser.getUserId(), Role.ADMIN, godUser.getUserId()));

            verify(authRepository).save(any(AuthenticationInfo.class));
        }
    }
}