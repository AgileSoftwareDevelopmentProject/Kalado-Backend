package com.kalado.common.feign.user;

import com.kalado.common.dto.AdminDto;
import com.kalado.common.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service")
public interface UserApi {
  @PutMapping("/user/modifyProfile")
  Boolean modifyUserProfile(@RequestParam("userId") Long userId, @RequestBody UserDto userDto);

  @GetMapping("/user/getProfile")
  UserDto getUserProfile(@RequestParam("userId") Long userId);

  @PostMapping("/user")
  void createUser(@RequestBody UserDto userDto);

  @PostMapping("/user/admin")
  void createAdmin(@RequestBody AdminDto adminDto);

  @PostMapping("/user/block/{userId}")
  boolean blockUser(@PathVariable Long userId);
}
