package com.sgg.common.exception;

public class SggError {

    SggError(){ };

    public SggError(SggException sggException) {
        this.errorMessage = sggException.getMessage();
    }
    public String errorMessage;

}
