package com.sgg.users;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Requires(env = "local")
public class LocalUserBootstrap {

    UserService userService;

    @Inject
    LocalUserBootstrap(UserService userService) {
        this.userService = userService;
    }

    @EventListener
    void onStartup(StartupEvent event) {
        log.info("loading local user bootstrapping data...");
        createTestUser("test_user_1");
        createTestUser("test_user_2");
        createTestUser("test_user_3");
    }

    private void createTestUser(String username) {
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest(
                username,
                "password123",
                "password123"
        );
        userService.registerUser(registrationRequest);
    }
}
