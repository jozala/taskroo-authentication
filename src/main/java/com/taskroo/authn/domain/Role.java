package com.taskroo.authn.domain;

public enum Role {
    ADMIN(0), USER(1);

    private final int intValue;

    private Role(int intValue) {
        this.intValue = intValue;
    }

    public int intValue() {
        return intValue;
    }
}