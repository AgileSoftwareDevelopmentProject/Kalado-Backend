package com.kalado.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.kalado.common.enums.Role;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequestDto {
    private String password;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private Role role;
}