package com.sgg.common;

import io.micronaut.context.annotation.Primary;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;
import io.micronaut.validation.exceptions.ConstraintExceptionHandler;

import javax.validation.ConstraintViolation;

@Primary
public class SggConstraintExceptionHandler extends ConstraintExceptionHandler {

    public SggConstraintExceptionHandler(ErrorResponseProcessor<?> responseProcessor) {
        super(responseProcessor);
    }

    /**
     * Overridden so that only the message is returned, and the property name is not included.
     */
    @Override
    protected String buildMessage(ConstraintViolation violation) {
        return violation.getMessage();
    }

}
