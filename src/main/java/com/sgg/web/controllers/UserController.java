package com.sgg.web.controllers;

import com.sgg.users.UserRegistrationRequest;
import com.sgg.users.UserService;
import com.sgg.users.ValidUserRegistration;
import com.sgg.users.model.UserDto;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.val;

import javax.validation.Valid;

@Controller("${apiVersion}/users")
@AllArgsConstructor(onConstructor_ = @Inject)
public class UserController {

    UserService userService;

    @Post("/register")
    @Secured(SecurityRule.IS_ANONYMOUS)
    public HttpResponse<UserDto> register(@Body @Valid @ValidUserRegistration
                                              UserRegistrationRequest userRegistrationRequest) {
        val result = userService.registerUser(userRegistrationRequest);
        return HttpResponse.status(HttpStatus.OK).body(result);
    }

}
