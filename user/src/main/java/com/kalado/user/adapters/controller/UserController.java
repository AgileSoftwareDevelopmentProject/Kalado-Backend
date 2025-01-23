package com.kalado.user.adapters.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalado.common.dto.AdminDto;
import com.kalado.common.dto.UserDto;
import com.kalado.common.dto.UserProfileUpdateDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.user.UserApi;
import com.kalado.user.service.ImageService;
import com.kalado.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController implements UserApi {
  private final UserService userService;
  private final ImageService imageService;
  private final ObjectMapper objectMapper;

  @PutMapping(value = "/user/modifyProfile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Boolean modifyUserProfile(
          @RequestParam("userId") Long userId,
          @RequestParam("profile") String profileJson,
          @RequestParam(value = "profileImage", required = false) MultipartFile profileImage
  ) {
    try {
      UserProfileUpdateDto profileData = objectMapper.readValue(profileJson, UserProfileUpdateDto.class);

      // Process profile update and image
      return userService.modifyProfile(userId, profileData, profileImage);

    } catch (JsonProcessingException e) {
      log.error("Error parsing profile JSON: {}", e.getMessage());
      throw new CustomException(
              ErrorCode.BAD_REQUEST,
              "Invalid profile data format: " + e.getMessage()
      );
    } catch (Exception e) {
      log.error("Error processing profile update: {}", e.getMessage(), e);
      throw new CustomException(
              ErrorCode.INTERNAL_SERVER_ERROR,
              "Failed to process profile update: " + e.getMessage()
      );
    }
  }

  @Override
  @GetMapping("/user/getProfile")
  public UserDto getUserProfile(@RequestParam("userId") Long userId) {
    return userService.getUserProfile(userId);
  }

  @Override
  @PostMapping("/user")
  public void createUser(@RequestBody UserDto userDto) {
    userService.createUser(userDto);
  }

  @Override
  @PostMapping("/user/admin")
  public void createAdmin(@RequestBody AdminDto adminDto) {
    userService.createAdmin(adminDto);
  }

  @Override
  @PostMapping("/user/block/{userId}")
  public boolean blockUser(@PathVariable Long userId) {
    return userService.blockUser(userId);
  }

  @Override
  @GetMapping(value = "/user/profile-image/{filename}", produces = MediaType.IMAGE_JPEG_VALUE)
  public Resource getProfileImage(@PathVariable String filename) {
    return imageService.getProfileImage(filename);
  }
}