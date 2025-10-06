package ru.yandex.practicum.filmorate.controller.exception;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
