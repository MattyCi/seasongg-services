package com.sgg.users;

import io.micronaut.core.annotation.Introspected;
import jakarta.validation.constraints.Size;
import lombok.Value;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Value
@Introspected
public class UserRegistrationRequest {

    @NotBlank(message = "{registration.username.NotBlank}")
    @Size(min = 3, max = 32, message = "{registration.username.Size}")
    @Pattern(regexp = "^[a-zA-Z0-9'-]*$", message = "{registration.username.Pattern}")
    String username;

    @NotBlank(message = "{registration.password.NotBlank}")
    @Size(min = 8, max = 32, message = "{registration.password.Size}")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "{registration.password.Pattern}")
    String password;

    @NotBlank(message = "{registration.passwordVerify.NotBlank}")
    String passwordVerify;
}
