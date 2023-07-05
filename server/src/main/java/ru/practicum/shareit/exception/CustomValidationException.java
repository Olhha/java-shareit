package ru.practicum.shareit.exception;

public class CustomValidationException extends RuntimeException {
    public CustomValidationException(String s) {
        super(s);
    }
}
