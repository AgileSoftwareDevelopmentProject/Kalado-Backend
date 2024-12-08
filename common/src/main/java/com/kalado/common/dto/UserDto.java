package com.kalado.common.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@Setter
@Getter
public class UserDto {
  private Long id;
  private String username;
  private String firstName;
  private String lastName;
  private String address;
  private String phoneNumber;
}