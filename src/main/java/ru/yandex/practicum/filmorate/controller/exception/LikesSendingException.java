package ru.yandex.practicum.filmorate.controller.exception;

public class LikesSendingException extends RuntimeException {
    public LikesSendingException(String message) {
        super(message);
    }
}
