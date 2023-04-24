package com.sgg.web.controllers;

import com.sgg.users.UserDTO;
import com.sgg.users.UserRegistrationRequest;
import com.sgg.users.UserService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

@Controller("${apiVersion}/users")
public class UserController {

    UserService userService;

    @Inject
    UserController(UserService userService) {
        this.userService = userService;
    }

    @Post("/register")
    @Secured(SecurityRule.IS_ANONYMOUS)
    public HttpResponse<UserDTO> register(@Body UserRegistrationRequest userRegistrationRequest) {
        UserDTO result = userService.registerUser(userRegistrationRequest);
        return HttpResponse.status(HttpStatus.OK).body(result);
    }

}
