package com.kalado.user.adapters.controller;

import com.kalado.common.dto.LocationDto;
import com.kalado.common.feign.user.UserApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

  private final UserService userService;

  @Override
  public Boolean modifyUserProfile(long id, UserDto userDto) {
    return UserService.modifyProfile(id, userDto);
  }

  @Override
  public UserDto getUserProfile(long id) {
    return UserService.getUserProfile(id);
  }

  @Override
  public void createUser(UserDto UserDto) {
    UserService.createUser(UserDto);
  }
}
