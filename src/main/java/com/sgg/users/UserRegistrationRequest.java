package com.sgg.users;

import io.micronaut.core.annotation.Introspected;
import lombok.Value;

import javax.validation.constraints.NotBlank;

@Value
@Introspected
public class UserRegistrationRequest {

    @NotBlank(message = "{registration.username.NotBlank}")
    String username;
    String password;
    String passwordVerify;
}
