package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.controller.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @GetMapping
    public Collection<User> getUsers() {
        return users.values();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        validate(user);

        if (user.getId() != null && users.containsKey(user.getId())) {
            log.warn("Пользователь не прошел валидацию по id (такой уже есть)");
            throw new ValidationException("Пользователь с id " + user.getId() + " уже существует");
        }

        if (user.getId() == null) {
            user.setId(getNextId());
        }

        users.put(user.getId(), user);

        log.info("Создан пользователь {}", user);
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        validate(user);

        if (user.getId() != null && !users.containsKey(user.getId())) {
            log.warn("Пользователь не прошел валидацию по id (такого нет)");
            throw new ValidationException("Пользователя с id " + user.getId() + " нет");
        }

        User updatedUser = user.toBuilder().build();
        users.put(user.getId(), updatedUser);

        log.info("Обновлен пользователь с id={}, {}", user.getId(), user);
        return updatedUser;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void validate(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Пользователь не прошел валидацию: пустая электронная почта");
            throw new ValidationException("Электронная почта не может быть пустой");
        }

        if (!user.getEmail().contains("@")) {
            log.warn("Пользователь не прошел валидацию: некорректная электронная почта {}", user.getEmail());
            throw new ValidationException("Электронная почта должна содержать символ '@'");
        }

        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("Пользователь не прошел валидацию: пустой логин");
            throw new ValidationException("Логин не может быть пустым");
        }

        if (user.getLogin().contains(" ")) {
            log.warn("Пользователь не прошел валидацию: логин содержит пробелы {}", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пользователя пустое, подставляем логин {}", user.getLogin());
            user.setName(user.getLogin());
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Пользователь не прошел валидацию: дата рождения в будущем {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}