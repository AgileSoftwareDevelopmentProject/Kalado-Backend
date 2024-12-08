package com.kalado.common.feign.user;

import com.kalado.common.dto.AdminDto;
import com.kalado.common.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service")
public interface UserApi {

  @PutMapping("/user/{id}")
  Boolean modifyUserProfile(@PathVariable long id, @RequestBody UserDto userDto);

  @GetMapping("/user/{userId}")
  UserDto getUserProfile(@PathVariable Long userId);

  @PostMapping("/user")
  void createUser(@RequestBody UserDto userDto);

  @PostMapping("/admin")
  void createAdmin(@RequestBody AdminDto adminDto);
}