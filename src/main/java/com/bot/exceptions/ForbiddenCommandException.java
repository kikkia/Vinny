package com.bot.exceptions;

public class ForbiddenCommandException extends Exception {

    public ForbiddenCommandException(String message) {
        super(message);
    }
}
