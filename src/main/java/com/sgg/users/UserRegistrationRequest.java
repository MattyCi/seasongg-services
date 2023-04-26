package com.sgg.users;

import lombok.Value;

@Value
public class UserRegistrationRequest {
    String username;
    String password;
    String passwordVerify;
}
