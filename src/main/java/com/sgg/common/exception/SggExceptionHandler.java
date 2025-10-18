package com.sgg.common.exception;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;

@Produces
@Singleton
@Requires(classes = {SggException.class, ExceptionHandler.class})
public class SggExceptionHandler implements ExceptionHandler<SggException, HttpResponse<SggError>> {

    @Override
    public HttpResponse<SggError> handle(HttpRequest request, SggException exception) {
        return HttpResponse.serverError(new SggError(exception));
    }
}
