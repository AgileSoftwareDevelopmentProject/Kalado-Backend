package com.kalado.gateway.adapters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalado.common.dto.UserDto;
import com.kalado.common.dto.UserProfileUpdateDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.user.UserApi;
import com.kalado.gateway.annotation.Authentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {
  private final UserApi userApi;
  private final ObjectMapper objectMapper;

  @PutMapping(value = "/modifyProfile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Authentication(userId = "#userId")
  public Boolean modifyUserProfile(
          Long userId,
          @RequestParam("profile") String profileJson,
          @RequestParam(value = "profileImage", required = false) MultipartFile profileImage
  ) {
    try {
      // Parse the profile JSON first, just like in ProductController
      UserProfileUpdateDto profileData = objectMapper.readValue(profileJson, UserProfileUpdateDto.class);

      // We could add any necessary modifications to the DTO here
      // Similar to how ProductController sets the sellerId

      // Convert back to JSON string if we made any modifications
      String updatedProfileJson = objectMapper.writeValueAsString(profileData);

      log.debug("Modifying profile for user ID: {} with data: {}", userId, profileData);
      log.debug("Profile image present: {}", profileImage != null);

      // Forward to the user service with consistent parameter types
      return userApi.modifyUserProfile(userId, updatedProfileJson, profileImage);

    } catch (JsonProcessingException e) {
      // Handle JSON parsing errors specifically, like ProductController does
      log.error("Error parsing profile JSON: {}", e.getMessage());
      throw new CustomException(
              ErrorCode.BAD_REQUEST,
              "Invalid profile data format: " + e.getMessage()
      );
    } catch (Exception e) {
      // Handle other errors
      log.error("Error modifying user profile for user {}: {}", userId, e.getMessage(), e);
      throw new CustomException(
              ErrorCode.INTERNAL_SERVER_ERROR,
              "Error processing profile update: " + e.getMessage()
      );
    }
  }
}