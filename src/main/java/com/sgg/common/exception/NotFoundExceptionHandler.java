package com.sgg.common.exception;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;

@Produces
@Singleton
@Requires(classes = {NotFoundException.class, ExceptionHandler.class})
public class NotFoundExceptionHandler
        implements ExceptionHandler<NotFoundException, HttpResponse<SggError>> {

    @Override
    public HttpResponse<SggError> handle(HttpRequest request, NotFoundException exception) {
        return HttpResponse.notFound(new SggError(exception));
    }
}
