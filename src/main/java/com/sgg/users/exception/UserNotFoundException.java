package com.sgg.users.exception;

import com.sgg.common.exception.SggException;

public class UserNotFoundException extends SggException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
