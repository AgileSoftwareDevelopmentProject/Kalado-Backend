package com.kalado.authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import com.kalado.common.dto.AuthDto;
import com.kalado.common.enums.Role;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.user.UserApi;
import com.kalado.authentication.application.service.AuthenticationService;
import com.kalado.authentication.domain.model.AuthenticationInfo;
import com.kalado.authentication.infrastructure.repository.AuthenticationRepository;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthenticationServiceIntegrationTest {

  @Autowired private AuthenticationService authenticationService;
  @Autowired private AuthenticationRepository authRepository;
  @Autowired private RedisTemplate<String, Long> redisTemplate;
  @Autowired private PasswordEncoder passwordEncoder;
  @MockBean private UserApi userApi;

  @BeforeEach
  void setUp() {
    authRepository.deleteAll();
    authRepository.flush();
  }

  @Test
  void login_ShouldThrowException_WhenCredentialsAreInvalid() {
    AuthenticationInfo authInfo =
        AuthenticationInfo.builder()
            .username("invaliduser")
            .password(passwordEncoder.encode("password"))
            .role(Role.USER)
            .build();
    authRepository.save(authInfo);

    CustomException exception =
        assertThrows(
            CustomException.class,
            () -> authenticationService.login("invaliduser", "wrongpassword"));

    assertEquals(
        "Invalid username or password",
        exception.getMessage(),
        "Exception message should indicate invalid credentials");
  }

  @Test
  void validateToken_ShouldReturnInvalidAuthDto_WhenTokenIsInvalid() {
    String invalidToken = "invalid.token.value";

    AuthDto authDto = authenticationService.validateToken(invalidToken);

    assertNotNull(authDto, "AuthDto should not be null");
    assertFalse(authDto.isValid(), "AuthDto should be invalid");
  }

  @Test
  void register_ShouldCreateNewUser_WhenDataIsValid() {
    String username = "newuser";
    String password = "newpassword";
    Role role = Role.USER;
    Mockito.doNothing().when(userApi).createUser(any());
    authenticationService.register(username, password, role);

    AuthenticationInfo savedUser = authRepository.findByUsername(username);
    assertNotNull(savedUser, "Saved user should not be null");
    assertTrue(
        passwordEncoder.matches(password, savedUser.getPassword()),
        "Password should match the encoded password");
    assertEquals(role, savedUser.getRole(), "Role should be USER");
  }

  @Test
  void register_ShouldThrowException_WhenUserAlreadyExists() {
    AuthenticationInfo existingUser =
        AuthenticationInfo.builder()
            .username("existinguser")
            .password(passwordEncoder.encode("password"))
            .role(Role.USER)
            .build();
    authRepository.save(existingUser);

    CustomException exception =
        assertThrows(
            CustomException.class,
            () -> authenticationService.register("existinguser", "newpassword", Role.USER));

    assertEquals(
        "User already exists",
        exception.getMessage(),
        "Exception message should indicate user already exists");
  }
}
