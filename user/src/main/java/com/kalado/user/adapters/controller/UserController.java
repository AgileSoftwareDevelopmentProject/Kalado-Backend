package com.kalado.user.adapters.controller;

import com.kalado.common.dto.AdminDto;
import com.kalado.common.dto.UserDto;
import com.kalado.common.feign.user.UserApi;
import com.kalado.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController implements UserApi {

  private final UserService userService;

  @Override
  @PutMapping("/{id}")
  public Boolean modifyUserProfile(@PathVariable long id, @RequestBody UserDto userDto) {
    return userService.modifyProfile(id, userDto);
  }

  @Override
  @GetMapping("/{userId}")
  public UserDto getUserProfile(@PathVariable Long userId) {
    return userService.getUserProfile(userId);
  }

  @Override
  @PostMapping
  public void createUser(@RequestBody UserDto userDto) {
    userService.createUser(userDto);
  }

  @Override
  @PostMapping("/admin")
  public void createAdmin(@RequestBody AdminDto adminDto) {
    userService.createAdmin(adminDto);
  }

  @Override
  public boolean blockUser(@PathVariable Long userId) {
    return userService.blockUser(userId);
  }
}