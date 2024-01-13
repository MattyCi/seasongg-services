package com.sgg.users.exception;

import com.sgg.common.exception.SggException;

public class ClientException extends SggException {
    public ClientException(String message) {
        super(message);
    }
}
