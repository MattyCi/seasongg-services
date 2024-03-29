package com.sgg.users;

import io.micronaut.core.annotation.Introspected;
import lombok.Value;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Value
@Introspected
public class UserRegistrationRequest {

    @NotBlank(message = "{registration.username.NotBlank}")
    @Pattern(regexp = "^[a-zA-Z0-9'-]*$", message = "{registration.username.Pattern}")
    String username;
    String password;
    String passwordVerify;
}
