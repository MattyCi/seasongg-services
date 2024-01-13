package com.sgg.users.exception;

import com.sgg.common.exception.SggException;

public class NotFoundException extends SggException {
    public NotFoundException(String message) {
        super(message);
    }
}
