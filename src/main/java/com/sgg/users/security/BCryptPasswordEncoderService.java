package com.sgg.users.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import jakarta.inject.Singleton;

/**
 * This class is merely a workaround so that we can inject a Spring Security implementation class in Micronaut.
 */
@Singleton
class BCryptPasswordEncoderService implements PasswordEncoder {
    org.springframework.security.crypto.password.PasswordEncoder delegate = new BCryptPasswordEncoder();

    @Override
    public String encode(String rawPassword) {
        return delegate.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return delegate.matches(rawPassword, encodedPassword);
    }
}