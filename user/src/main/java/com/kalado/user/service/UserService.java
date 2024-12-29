package com.kalado.user.service;

import com.kalado.common.dto.AdminDto;
import com.kalado.common.dto.UserDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.authentication.AuthenticationApi;
import com.kalado.user.domain.mapper.UserMapper;
import java.util.Optional;
import com.kalado.user.adapters.repository.UserRepository;

import com.kalado.user.domain.model.Admin;
import com.kalado.user.domain.model.User;
import com.kalado.user.domain.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

  private final UserRepository userRepository;
  private final AuthenticationApi authenticationApi;
  private final AdminRepository adminRepository;

  public Boolean modifyProfile(long id, UserDto userDto) {
    Optional<User> userOptional = userRepository.findById(id);

    if (userOptional.isPresent()) {
      log.info("Modifying profile for user ID: {}", id);
      User user = userOptional.get();
      userRepository.modify(
          userDto.getFirstName(),
          userDto.getLastName(),
          userDto.getAddress(),
          userDto.getPhoneNumber(),
          user.getId(),
          false);
      log.info("Profile modified successfully for user ID: {}", id);
      return true;
    } else {
      log.info("user ID: {} not found, creating new user", id);
      User newuser = UserMapper.INSTANCE.dtoTouser(userDto);
      newuser.setId(id);
      userRepository.save(newuser);
      log.info("New user created with ID: {}", id);
      return true;
    }
  }

  public void createUser(UserDto userDto) {
    log.info("Creating a new user with id: {}", userDto.getId());
    User newuser = UserMapper.INSTANCE.dtoTouser(userDto);
    userRepository.save(newuser);
    log.info("Successfully created user with ID: {}", newuser.getId());
  }

  public String getUserAddress(long userID) {
    log.info("Retrieving address for user ID: {}", userID);
    return userRepository
        .findById(userID)
        .map(User::getAddress)
        .orElseThrow(
            () -> {
              log.error("user ID: {} not found", userID);
              return new CustomException(ErrorCode.NOT_FOUND, "user not found");
            });
  }

  public UserDto getUserProfile(long userId) {
    log.info("Retrieving user profile for user ID: {}", userId);
    String username = authenticationApi.getUsername(userId);
    return userRepository
        .findById(userId)
        .map(
            user -> {
              UserDto userDto = UserMapper.INSTANCE.userToDto(user);
              userDto.setUsername(username);
              return userDto;
            })
        .orElseThrow(
            () -> {
              log.error("user ID: {} not found", userId);
              return new CustomException(ErrorCode.NOT_FOUND, "user not found");
            });
  }

  public void createAdmin(AdminDto adminDto) {
    log.info("Creating a new admin with id: {}", adminDto.getId());
    Admin newAdmin = UserMapper.INSTANCE.dtoToAdmin(adminDto);
    adminRepository.save(newAdmin);
    log.info("Successfully created admin with ID: {}", newAdmin.getId());
  }

  public boolean blockUser(Long id) {
    Optional<User> userOptional = userRepository.findById(id);

    if (userOptional.isPresent()) {
      log.info("Modifying profile for user ID: {}", id);
      User user = userOptional.get();
      userRepository.modify(
          user.getFirstName(),
          user.getLastName(),
          user.getAddress(),
          user.getPhoneNumber(),
          user.getId(),
          true);
      log.info("User is blocked. user ID: {}", id);
      return true;
    } else {
      log.info("user ID: {} not found, creating new user", id);
      return false;
    }
  }
}
