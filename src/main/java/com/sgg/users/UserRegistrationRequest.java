package com.sgg.users;

import io.micronaut.core.annotation.Introspected;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Value
@Introspected
public class UserRegistrationRequest {

    @NotBlank(message = "{registration.username.NotBlank}")
    @Pattern(regexp = "[^A-Za-z0-9'-]", message = "{registration.username.Pattern}")
    String username;
    String password;
    String passwordVerify;
}
