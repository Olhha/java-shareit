package ru.practicum.shareit.exception;

public class UpdateForbiddenException extends RuntimeException {
    public UpdateForbiddenException(String s) {
        super(s);
    }
}
