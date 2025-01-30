package com.kalado.authentication.application.service;

import com.kalado.authentication.domain.model.AuthenticationInfo;
import com.kalado.common.dto.AuthDto;
import com.kalado.common.dto.UserDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.enums.Role;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.user.UserApi;
import com.kalado.common.response.LoginResponse;
import com.kalado.authentication.infrastructure.repository.AuthenticationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
  private final AuthenticationRepository authRepository;
  private final UserApi userApi;
  private final VerificationService verificationService;
  private final TokenService tokenService;
  private final PasswordResetService passwordResetService;
  private final RoleService roleService;

  public AuthenticationInfo findByUsername(String username) {
    return authRepository.findByUsername(username);
  }

  public LoginResponse login(String username, String password) {
    validateLoginInput(username, password);

    AuthenticationInfo authInfo = authRepository.findByUsername(username);
    if (authInfo == null || !passwordResetService.verifyPassword(authInfo, password)) {
      log.warn("Invalid login attempt for username: {}", username);
      throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Invalid username or password");
    }

    if (!verificationService.isEmailVerified(authInfo)) {
      log.warn("Email not verified for username: {}", username);
      throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED, "Email not verified");
    }

    validateUserStatus(authInfo);

    String token = tokenService.generateToken(authInfo.getUserId());
    return LoginResponse.builder()
            .token(token)
            .role(authInfo.getRole())
            .build();
  }

  private void validateUserStatus(AuthenticationInfo authInfo) {
    if (authInfo.getRole() == Role.USER) {
      try {
        UserDto userDto = userApi.getUserProfile(authInfo.getUserId());
        if (userDto != null && userDto.isBlocked()) {
          log.warn("Blocked user attempted to login: {}", authInfo.getUsername());
          throw new CustomException(ErrorCode.UNAUTHORIZED, "Your account has been blocked");
        }
      } catch (Exception e) {
        log.error("Error checking user blocked status: {}", e.getMessage());
        if (e instanceof CustomException) {
          throw e;
        }
        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "Error validating user status");
      }
    }
  }

  public AuthDto validateToken(String tokenValue) {
    return tokenService.validateTokenAndCreateAuthDto(tokenValue, authRepository);
  }

  public void invalidateToken(String token) {
    tokenService.invalidateToken(token);
  }

  @Transactional
  public void updateUserRole(Long userId, Role newRole, Long requestingUserId) {
    roleService.updateUserRole(userId, newRole, requestingUserId);
  }

  private void validateLoginInput(String username, String password) {
    if (username == null || username.isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Username cannot be empty");
    }
    if (password == null || password.isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Password cannot be empty");
    }
  }

  public String getUsername(Long userId) {
    return authRepository.findById(userId)
            .map(AuthenticationInfo::getUsername)
            .orElse(null);
  }

  public Optional<AuthenticationInfo> findUserById(Long userId) {
    return authRepository.findById(userId);
  }
}