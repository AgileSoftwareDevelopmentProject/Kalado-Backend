package com.kalado.authentication.adapters.controller;

import com.kalado.common.dto.AuthDto;
import com.kalado.common.enums.Role;
import com.kalado.common.feign.authentication.AuthenticationApi;
import com.kalado.common.response.LoginResponse;
import com.kalado.authentication.application.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthenticationController implements AuthenticationApi {

  private final AuthenticationService authService;

  @Override
  public LoginResponse login(String username, String password) {
    return authService.login(username, password);
  }

  @Override
  public AuthDto validate(String token) {
    return authService.validateToken(token);
  }

  @Override
  public String getUsername(Long userId) {
    return authService.getUsername(userId);
  }

  @Override
  public void logout(String token) {
    authService.invalidateToken(token);
  }

  @Override
  public void register(String username, String password, Role role) {
    authService.register(username, password, role);
  }
}
