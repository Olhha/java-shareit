package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(final MethodArgumentNotValidException e) {
        log.debug("Получен статус 400 Bad request: {}", e.getMessage(), e);
        return new ErrorResponse(
                "Произошла ошибка валидации запроса."
                        + e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBookingValidationException(final CustomValidationException e) {
        log.debug("Получен статус 400 Bad request: {}", e.getMessage(), e);
        return new ErrorResponse(
                "Произошла ошибка валидации запроса."
                        + e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(final NotFoundException e) {
        log.debug("Получен статус 404 Not found: {}", e.getMessage(), e);
        return new ErrorResponse(
                e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleUpdateForbiddenException(final UpdateForbiddenException e) {
        log.debug("Получен статус 403 Forbidden: {}", e.getMessage(), e);
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowable(final Throwable e) {
        log.debug("Получен статус 500 Internal Server Error: {}", e.getMessage(), e);
        return new ErrorResponse(
                "Произошла непредвиденная ошибка на сервере."
                        + e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleUniqueConstraintViolation(final DataIntegrityViolationException e) {
        log.debug("Получен статус 409 Conflict: {}", e.getMessage(), e);
        return new ErrorResponse(e.getMessage());
    }

}
