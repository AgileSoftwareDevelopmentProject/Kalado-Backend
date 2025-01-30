package com.kalado.authentication.application.service;

import com.kalado.authentication.domain.model.AuthenticationInfo;
import com.kalado.authentication.infrastructure.repository.AuthenticationRepository;
import com.kalado.common.dto.AdminDto;
import com.kalado.common.dto.RegistrationRequestDto;
import com.kalado.common.dto.UserDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.enums.Role;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.user.UserApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {
    private final AuthenticationRepository authRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserApi userApi;
    private final VerificationService verificationService;
    private final RoleService roleService;

    @Transactional
    public AuthenticationInfo register(RegistrationRequestDto request) {
        validateRegistrationInput(request);

        if (request.getRole() == Role.GOD || request.getRole() == Role.ADMIN) {
            roleService.validatePrivilegedRegistration(request.getEmail(), request.getRole());
        }

        AuthenticationInfo existingUser = authRepository.findByUsername(request.getEmail());
        if (existingUser != null) {
            log.info("User already exists: {}", existingUser);
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS, "User already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        AuthenticationInfo authenticationInfo = createAuthenticationInfo(request, encodedPassword);
        createUserProfile(authenticationInfo, request);
        verificationService.createVerificationToken(authenticationInfo);

        return authenticationInfo;
    }

    private AuthenticationInfo createAuthenticationInfo(RegistrationRequestDto request, String encodedPassword) {
        return authRepository.save(
                AuthenticationInfo.builder()
                        .username(request.getEmail())
                        .password(encodedPassword)
                        .role(request.getRole())
                        .build());
    }

    private void createUserProfile(AuthenticationInfo authInfo, RegistrationRequestDto request) {
        UserDto userDto = UserDto.builder()
                .id(authInfo.getUserId())
                .username(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .build();

        switch (request.getRole()) {
            case GOD, ADMIN -> createAdminProfile(authInfo.getUserId(), request);
            case USER -> userApi.createUser(userDto);
        }
    }

    private void createAdminProfile(Long userId, RegistrationRequestDto request) {
        userApi.createAdmin(AdminDto.builder()
                .id(userId)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .build());
    }

    private void validateRegistrationInput(RegistrationRequestDto request) {
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Username cannot be empty");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Password cannot be empty");
        }
        if (request.getFirstName() == null || request.getFirstName().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "First name cannot be empty");
        }
        if (request.getLastName() == null || request.getLastName().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Last name cannot be empty");
        }
        if (request.getPhoneNumber() == null || request.getPhoneNumber().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Phone number cannot be empty");
        }
        if (request.getRole() == null) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Role cannot be null");
        }
    }
}