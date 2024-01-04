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
@Requires(classes = {UserNotFoundException.class, ExceptionHandler.class})
public class UserNotFoundExceptionHandler
        implements ExceptionHandler<UserNotFoundException, HttpResponse<SggError>> {

    @Override
    public HttpResponse<SggError> handle(HttpRequest request, UserNotFoundException exception) {
        return HttpResponse.notFound(new SggError(exception));
    }
}