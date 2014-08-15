package com.taskroo.authn.data;

public class NonExistingResourceOperationException extends Exception {
    public NonExistingResourceOperationException(String message) {
        super(message);
    }
}
