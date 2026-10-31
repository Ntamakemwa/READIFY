package com.readify.auth.entity;

public enum AccountType {
    READER,
    SCHOOL_LIBRARY,
    PRIVATE_LIBRARY;

    public boolean canLend() {
        return this != READER;
    }

    public boolean canSell() {
        return this == PRIVATE_LIBRARY;
    }
}