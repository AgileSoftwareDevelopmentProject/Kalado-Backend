package com.kalado.user.service;

import com.kalado.common.enums.ErrorCode;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.authentication.AuthenticationApi;
import com.kalado.user.adapters.repository.userRepository;
import com.kalado.user.domain.mapper.UserMapper;
import java.util.Optional;

import com.kalado.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class userService {

  private final UserRepository userRepository;
  private final AuthenticationApi authenticationApi;

  public Boolean modifyProfile(long id, userDto userDto) {
    Optional<user> userOptional = userRepository.findById(id);

    if (userOptional.isPresent()) {
      log.info("Modifying profile for user ID: {}", id);
      user user = userOptional.get();
      userRepository.modify(
          userDto.getFirstName(),
          userDto.getLastName(),
          userDto.getAddress(),
          userDto.getPhoneNumber(),
          user.getId());
      log.info("Profile modified successfully for user ID: {}", id);
      return true;
    } else {
      log.info("user ID: {} not found, creating new user", id);
      user newuser = UserMapper.INSTANCE.dtoTouser(userDto);
      newuser.setId(id);
      userRepository.save(newuser);
      log.info("New user created with ID: {}", id);
      return true;
    }
  }

  public void createuser(userDto userDto) {
    log.info("Creating a new user with id: {}", userDto.getId());
    user newuser = UserMapper.INSTANCE.dtoTouser(userDto);
    userRepository.save(newuser);
    log.info("Successfully created user with ID: {}", newuser.getId());
  }

  public String getuserAddress(long userID) {
    log.info("Retrieving address for user ID: {}", userID);
    return userRepository
        .findById(userID)
        .map(user::getAddress)
        .orElseThrow(
            () -> {
              log.error("user ID: {} not found", userID);
              return new CustomException(ErrorCode.NOT_FOUND, "user not found");
            });
  }

  public userDto getUserProfile(long userId) {
    log.info("Retrieving user profile for user ID: {}", userId);
    String username = authenticationApi.getUsername(userId);
    return userRepository
        .findById(userId)
        .map(
            user -> {
              userDto userDto = UserMapper.INSTANCE.userToDto(user);
              userDto.setUsername(username);
              return userDto;
            })
        .orElseThrow(
            () -> {
              log.error("user ID: {} not found", userId);
              return new CustomException(ErrorCode.NOT_FOUND, "user not found");
            });
  }
}
