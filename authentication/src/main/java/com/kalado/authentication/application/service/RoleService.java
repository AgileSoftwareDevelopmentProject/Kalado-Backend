package com.kalado.authentication.application.service;

import com.kalado.authentication.configuration.AdminConfiguration;
import com.kalado.authentication.domain.model.AuthenticationInfo;
import com.kalado.authentication.infrastructure.repository.AuthenticationRepository;
import com.kalado.common.dto.AdminDto;
import com.kalado.common.dto.UserDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.enums.Role;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.user.UserApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {
    private final AuthenticationRepository authRepository;
    private final UserApi userApi;
    private final AdminConfiguration adminConfig;

    @Transactional
    public void updateUserRole(Long userId, Role newRole, Long requestingUserId) {
        AuthenticationInfo requestingUser = findAndValidateRequestingUser(requestingUserId);
        AuthenticationInfo targetUser = findAndValidateTargetUser(userId);

        validateRoleChange(requestingUser.getRole(), targetUser.getRole(), newRole);
        updateRole(targetUser, newRole);
        handleRoleSpecificActions(targetUser, newRole);
    }

    private AuthenticationInfo findAndValidateRequestingUser(Long requestingUserId) {
        return authRepository.findById(requestingUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "Requesting user not found"));
    }

    private AuthenticationInfo findAndValidateTargetUser(Long userId) {
        return authRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "Target user not found"));
    }

    private void validateRoleChange(Role requestingRole, Role currentRole, Role newRole) {
        validateGodPrivilege(requestingRole);
        validateTargetRoleRestrictions(currentRole);
        validateRoleTransition(currentRole, newRole);
    }

    private void validateGodPrivilege(Role requestingRole) {
        if (requestingRole != Role.GOD) {
            throw new CustomException(ErrorCode.FORBIDDEN, "Only GOD role can modify user roles");
        }
    }

    private void validateTargetRoleRestrictions(Role currentRole) {
        if (currentRole == Role.GOD) {
            throw new CustomException(ErrorCode.FORBIDDEN, "Cannot modify GOD role");
        }
    }

    private void validateRoleTransition(Role currentRole, Role newRole) {
        boolean isValidTransition = switch (currentRole) {
            case USER -> newRole == Role.ADMIN;  // Users can only be promoted to ADMIN
            case ADMIN -> newRole == Role.USER;  // Admins can only be demoted to USER
            default -> false;
        };

        if (!isValidTransition) {
            throw new CustomException(
                    ErrorCode.INVALID_ROLE_TRANSITION,
                    "Invalid role transition from " + currentRole + " to " + newRole
            );
        }
    }

    private void updateRole(AuthenticationInfo user, Role newRole) {
        user.setRole(newRole);
        authRepository.save(user);
        log.info("Updated role for user {} to {}", user.getUserId(), newRole);
    }

    private void handleRoleSpecificActions(AuthenticationInfo user, Role newRole) {
        if (newRole == Role.ADMIN) {
            promoteToAdmin(user.getUserId());
        }
    }

    private void promoteToAdmin(Long userId) {
        UserDto userProfile = userApi.getUserProfile(userId);
        userApi.createAdmin(AdminDto.builder()
                .id(userId)
                .firstName(userProfile.getFirstName())
                .lastName(userProfile.getLastName())
                .phoneNumber(userProfile.getPhoneNumber())
                .build());
        log.info("Created admin profile for user {}", userId);
    }

    public void validatePrivilegedRegistration(String email, Role role) {
        if (role == Role.GOD) {
            validateGodRegistration(email);
        } else if (role == Role.ADMIN) {
            validateAdminRegistration(email);
        }
    }

    private void validateGodRegistration(String email) {
        if (!adminConfig.isEmailAuthorizedForGod(email)) {
            log.warn("Unauthorized attempt to register as GOD: {}", email);
            throw new CustomException(
                    ErrorCode.FORBIDDEN,
                    "GOD registration is restricted to authorized emails only"
            );
        }
    }

    private void validateAdminRegistration(String email) {
        if (!adminConfig.isEmailAuthorizedForAdmin(email)) {
            log.warn("Unauthorized attempt to register as admin: {}", email);
            throw new CustomException(
                    ErrorCode.FORBIDDEN,
                    "Admin registration is restricted to authorized emails only"
            );
        }
    }
}