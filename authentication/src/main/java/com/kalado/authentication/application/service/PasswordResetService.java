package com.kalado.authentication.application.service;

import com.kalado.authentication.domain.model.AuthenticationInfo;
import com.kalado.authentication.domain.model.PasswordResetToken;
import com.kalado.authentication.infrastructure.repository.AuthenticationRepository;
import com.kalado.authentication.infrastructure.repository.PasswordResetTokenRepository;
import com.kalado.common.dto.ResetPasswordRequestDto;
import com.kalado.common.dto.ResetPasswordResponseDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {
    private final AuthenticationRepository authRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    private static final int EXPIRATION_HOURS = 24;

    @Transactional
    public void createPasswordResetTokenForUser(String email) {
        AuthenticationInfo user = authRepository.findByUsername(email);
        if (user == null) {
            // For security reasons, we still return success even if the email doesn't exist
            log.warn("Password reset requested for non-existent user: {}", email);
            return;
        }

        String token = generateToken();
        deleteExistingTokens(user);
        createAndSaveToken(user, token);
        emailService.sendPasswordResetToken(email, token);

        log.info("Password reset token created for user: {}", email);
    }

    private void deleteExistingTokens(AuthenticationInfo user) {
        tokenRepository.findAll().stream()
                .filter(t -> t.getUser().getUserId() == user.getUserId())
                .forEach(tokenRepository::delete);
    }

    private void createAndSaveToken(AuthenticationInfo user, String token) {
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(EXPIRATION_HOURS))
                .build();

        tokenRepository.save(resetToken);
    }

    @Transactional
    public ResetPasswordResponseDto resetPassword(ResetPasswordRequestDto request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN, "Invalid or expired token"));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new CustomException(ErrorCode.INVALID_TOKEN, "Token has expired");
        }

        updatePassword(resetToken.getUser(), request.getNewPassword());
        tokenRepository.delete(resetToken);

        log.info("Password successfully reset for user ID: {}", resetToken.getUser().getUserId());

        return ResetPasswordResponseDto.builder()
                .success(true)
                .message("Password has been reset successfully")
                .build();
    }

    @Transactional
    public void updatePassword(Long userId, String currentPassword, String newPassword) {
        AuthenticationInfo user = authRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "User not found"));

        validateCurrentPassword(user, currentPassword);
        updatePassword(user, newPassword);
        tokenService.invalidateUserTokens(userId);

        log.info("Password updated successfully for user ID: {}", userId);
    }

    private void validateCurrentPassword(AuthenticationInfo user, String currentPassword) {
        if (!verifyPassword(user, currentPassword)) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Current password is incorrect");
        }
    }

    @Transactional
    public void updatePassword(AuthenticationInfo user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        authRepository.save(user);
    }

    public boolean verifyPassword(AuthenticationInfo user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }
}