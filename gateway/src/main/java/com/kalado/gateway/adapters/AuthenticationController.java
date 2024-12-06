package com.kalado.gateway.adapters;

import com.kalado.common.feign.authentication.AuthenticationApi;
import com.kalado.common.response.LoginResponse;
import com.kalado.gateway.annotation.Authentication;
import com.kalado.gateway.request.LoginRequest;
import com.kalado.gateway.request.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
  private final AuthenticationApi authenticationApi;

  @PostMapping("/login")
  public LoginResponse login(@RequestBody LoginRequest loginRequest) {
    return authenticationApi.login(loginRequest.getUsername(), loginRequest.getPassword());
  }

  @PostMapping("/register")
  public void register(@RequestBody RegisterRequest registerRequest) {
    authenticationApi.register(
        registerRequest.getUsername(), registerRequest.getPassword(), registerRequest.getRole());
  }

  @GetMapping("/validate")
  @Authentication(token = "#token")
  boolean validate(String token) {
    return authenticationApi.validate(token).isValid();
  }

  @PostMapping("/logout")
  @Authentication(token = "#token")
  public void logout(String token) {
    authenticationApi.logout(token);
  }
}
