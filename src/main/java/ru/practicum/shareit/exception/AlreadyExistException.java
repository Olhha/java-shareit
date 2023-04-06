package ru.practicum.shareit.exception;

public class AlreadyExistException extends RuntimeException {
    public AlreadyExistException(String s) {
        super(s);
    }
}
