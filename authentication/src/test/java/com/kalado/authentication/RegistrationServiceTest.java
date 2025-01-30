package com.kalado.authentication;

import com.kalado.authentication.application.service.PasswordResetService;
import com.kalado.authentication.application.service.RegistrationService;
import com.kalado.authentication.application.service.RoleService;
import com.kalado.authentication.application.service.VerificationService;
import com.kalado.authentication.domain.model.AuthenticationInfo;
import com.kalado.authentication.infrastructure.repository.AuthenticationRepository;
import com.kalado.common.dto.AdminDto;
import com.kalado.common.dto.RegistrationRequestDto;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private AuthenticationRepository authRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private UserApi userApi;

    @Mock
    private VerificationService verificationService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RegistrationService registrationService;

    @Captor
    private ArgumentCaptor<AuthenticationInfo> authInfoCaptor;

    @Captor
    private ArgumentCaptor<UserDto> userDtoCaptor;

    @Captor
    private ArgumentCaptor<AdminDto> adminDtoCaptor;

    private RegistrationRequestDto validUserRequest;
    private AuthenticationInfo savedAuthInfo;
    private static final String ENCODED_PASSWORD = "encoded_password";
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        validUserRequest = RegistrationRequestDto.builder()
                .email("test@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("1234567890")
                .role(Role.USER)
                .build();

        savedAuthInfo = AuthenticationInfo.builder()
                .userId(USER_ID)
                .username(validUserRequest.getEmail())
                .password(ENCODED_PASSWORD)
                .role(Role.USER)
                .build();
    }

    @Nested
    @DisplayName("User Registration Tests")
    class UserRegistrationTests {
        @Test
        @DisplayName("Should successfully register new user")
        void register_Success() {
            // Arrange
            when(authRepository.findByUsername(validUserRequest.getEmail())).thenReturn(null);
            when(passwordEncoder.encode(validUserRequest.getPassword())).thenReturn(ENCODED_PASSWORD);
            when(authRepository.save(any(AuthenticationInfo.class))).thenReturn(savedAuthInfo);
            doNothing().when(userApi).createUser(any(UserDto.class));
            doNothing().when(verificationService).createVerificationToken(any(AuthenticationInfo.class));

            // Act
            AuthenticationInfo result = registrationService.register(validUserRequest);

            // Assert
            assertNotNull(result);
            assertEquals(USER_ID, result.getUserId());
            assertEquals(validUserRequest.getEmail(), result.getUsername());
            assertEquals(ENCODED_PASSWORD, result.getPassword());
            assertEquals(Role.USER, result.getRole());

            verify(userApi).createUser(userDtoCaptor.capture());
            UserDto capturedUserDto = userDtoCaptor.getValue();
            assertEquals(USER_ID, capturedUserDto.getId());
            assertEquals(validUserRequest.getEmail(), capturedUserDto.getUsername());
            assertEquals(validUserRequest.getFirstName(), capturedUserDto.getFirstName());
            assertEquals(validUserRequest.getLastName(), capturedUserDto.getLastName());
            assertEquals(validUserRequest.getPhoneNumber(), capturedUserDto.getPhoneNumber());

            verify(verificationService).createVerificationToken(result);
        }

        @Test
        @DisplayName("Should fail when user already exists")
        void register_UserExists() {
            // Arrange
            when(authRepository.findByUsername(validUserRequest.getEmail())).thenReturn(savedAuthInfo);

            // Act & Assert
            CustomException exception = assertThrows(CustomException.class,
                    () -> registrationService.register(validUserRequest));

            assertEquals(ErrorCode.USER_ALREADY_EXISTS, exception.getErrorCode());
            verify(authRepository, never()).save(any());
            verify(userApi, never()).createUser(any());
            verify(verificationService, never()).createVerificationToken(any());
        }
    }

    @Nested
    @DisplayName("Admin Registration Tests")
    class AdminRegistrationTests {

        private RegistrationRequestDto adminRequest;

        @BeforeEach
        void setUp() {
            adminRequest = validUserRequest.toBuilder()
                    .role(Role.ADMIN)
                    .build();
        }

        @Test
        @DisplayName("Should successfully register new admin")
        void register_AdminSuccess() {
            // Arrange
            AuthenticationInfo savedAdmin = savedAuthInfo.toBuilder().role(Role.ADMIN).build();
            when(authRepository.findByUsername(adminRequest.getEmail())).thenReturn(null);
            when(passwordEncoder.encode(adminRequest.getPassword())).thenReturn(ENCODED_PASSWORD);
            when(authRepository.save(any(AuthenticationInfo.class))).thenReturn(savedAdmin);
            doNothing().when(roleService).validatePrivilegedRegistration(adminRequest.getEmail(), Role.ADMIN);

            // Act
            AuthenticationInfo result = registrationService.register(adminRequest);

            // Assert
            assertNotNull(result);
            assertEquals(Role.ADMIN, result.getRole());
            verify(userApi).createAdmin(adminDtoCaptor.capture());
            verify(roleService).validatePrivilegedRegistration(adminRequest.getEmail(), Role.ADMIN);

            AdminDto capturedAdminDto = adminDtoCaptor.getValue();
            assertEquals(USER_ID, capturedAdminDto.getId());
            assertEquals(adminRequest.getFirstName(), capturedAdminDto.getFirstName());
            assertEquals(adminRequest.getLastName(), capturedAdminDto.getLastName());
        }

        @Test
        @DisplayName("Should fail when admin email not authorized")
        void register_UnauthorizedAdmin() {
            // Arrange
            doThrow(new CustomException(ErrorCode.FORBIDDEN, "Admin registration not allowed"))
                    .when(roleService).validatePrivilegedRegistration(adminRequest.getEmail(), Role.ADMIN);

            // Act & Assert
            CustomException exception = assertThrows(CustomException.class,
                    () -> registrationService.register(adminRequest));

            assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
            verify(authRepository, never()).save(any());
            verify(userApi, never()).createAdmin(any());
        }
    }

    @Nested
    @DisplayName("Input Validation Tests")
    class InputValidationTests {
        @Test
        @DisplayName("Should validate all required fields")
        void validateRegistrationInput() {
            // Email validation
            assertThrows(CustomException.class, () -> registrationService.register(
                    validUserRequest.toBuilder().email(null).build()));
            assertThrows(CustomException.class, () -> registrationService.register(
                    validUserRequest.toBuilder().email("").build()));

            // Password validation
            assertThrows(CustomException.class, () -> registrationService.register(
                    validUserRequest.toBuilder().password(null).build()));
            assertThrows(CustomException.class, () -> registrationService.register(
                    validUserRequest.toBuilder().password("").build()));

            // Other fields validation
            assertThrows(CustomException.class, () -> registrationService.register(
                    validUserRequest.toBuilder().firstName(null).build()));
            assertThrows(CustomException.class, () -> registrationService.register(
                    validUserRequest.toBuilder().lastName(null).build()));
            assertThrows(CustomException.class, () -> registrationService.register(
                    validUserRequest.toBuilder().phoneNumber(null).build()));
            assertThrows(CustomException.class, () -> registrationService.register(
                    validUserRequest.toBuilder().role(null).build()));
        }
    }
}