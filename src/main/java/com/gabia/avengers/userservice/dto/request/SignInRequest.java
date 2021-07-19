package com.gabia.avengers.userservice.dto.request;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignInRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}