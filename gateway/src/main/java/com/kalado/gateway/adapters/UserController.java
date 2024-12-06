package com.kalado.gateway.adapters;

import com.kalado.common.feign.user.UserApi;
import com.kalado.gateway.annotation.Authentication;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
public class UserController {
  private final UserApi userApi;

  @PutMapping
  @Authentication(userId = "#userId")
  Boolean modifyUserProfile(Long userId, @RequestBody UserDto userDto) {
    return userApi.modifyUserProfile(userId, userDto);
  }

  @GetMapping()
  @Authentication(userId = "#userId")
  public UserDto getUserProfile(Long userId) {
    return userApi.getUserProfile(userId);
  }

}
