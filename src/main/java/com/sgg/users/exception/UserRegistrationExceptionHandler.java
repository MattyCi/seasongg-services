package com.sgg.users.exception;

import com.sgg.common.exception.SggError;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;

@Produces
@Singleton
@Requires(classes = {UserRegistrationException.class, ExceptionHandler.class})
public class UserRegistrationExceptionHandler
        implements ExceptionHandler<UserRegistrationException, HttpResponse<SggError>> {

    @Override
    public HttpResponse<SggError> handle(HttpRequest request, UserRegistrationException exception) {
        return HttpResponse.badRequest(new SggError(exception));
    }
}
