package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.exception.FriendsAddingException;
import ru.yandex.practicum.filmorate.controller.exception.NotFoundException;
import ru.yandex.practicum.filmorate.controller.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserStorage userStorage;
    @Autowired
    private FeedStorage feedStorage;

    public Collection<User> getUsers() {
        return userStorage.getUsers().values();
    }

    public User getUser(Long id) {
        User user = userStorage.getUser(id);
        if (user == null) {
            log.warn("Не найден пользователь с id {}", id);
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        return user;
    }

    public User addUser(User user) {
        validate(user);

        userStorage.addUser(user);
        log.info("Создан пользователь {}", user);
        return user;
    }

    public User updateUser(User user) {
        if (user.getId() == null) {
            log.warn("Обновление пользователя без указания id");
            throw new ValidationException("ID не указан");
        }

        validate(user);

        if (userStorage.getUser(user.getId()) == null) {
            log.warn("Не найден пользователь для обновления с id {}", user.getId());
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }

        userStorage.updateUser(user.getId(), user);
        log.info("Обновлен пользователь с id={}, {}", user.getId(), user);
        return user;
    }

    public User addFriend(Long id, Long friendId) {
        User user = userStorage.getUser(id);
        User friend = userStorage.getUser(friendId);

        if (user == null) {
            log.warn("Попытка добавить друга: не найден пользователь {}", id);
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        if (friend == null) {
            log.warn("Попытка добавить в друзья несуществующего пользователя {}", friendId);
            throw new NotFoundException("Пользователь с id " + friendId + " не найден");
        }
        if (id.equals(friendId)) {
            log.warn("Пользователь {} пытается добавить себя в друзья", id);
            throw new FriendsAddingException("Пользователь не может добавить себя в друзья");
        }

        userStorage.addFriend(id, friendId);
        feedStorage.saveEvent(id, EventType.FRIEND, Operation.ADD, friendId);
        log.info("Пользователь {} добавил в друзья {}", id, friendId);
        return userStorage.getUser(id);
    }

    public User deleteFriend(Long id, Long friendId) {
        User user = userStorage.getUser(id);
        User friend = userStorage.getUser(friendId);

        if (user == null) {
            log.warn("Попытка удалить друга: не найден пользователь {}", id);
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        if (friend == null) {
            log.warn("Попытка удалить друга: не найден пользователь {}", friendId);
            throw new NotFoundException("Пользователь с id " + friendId + " не найден");
        }
        if (id.equals(friendId)) {
            log.warn("Пользователь {} пытается удалить себя из друзей", id);
            throw new FriendsAddingException("Пользователь пытается удалить себя из друзей");
        }

        userStorage.deleteFriend(id, friendId);
        feedStorage.saveEvent(id, EventType.FRIEND, Operation.REMOVE, friendId);
        log.info("Пользователь {} удалил из друзей {}", id, friendId);
        return userStorage.getUser(id);
    }

    public Set<User> getFriends(Long id) {
        User user = userStorage.getUser(id);
        if (user == null) {
            log.warn("Попытка получить список друзей несуществующего пользователя {}", id);
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }

        return user.getFriends().keySet().stream().map(userStorage::getUser).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public Set<User> getCommonFriends(Long id, Long otherId) {
        User user = userStorage.getUser(id);
        User other = userStorage.getUser(otherId);

        if (user == null) {
            log.warn("Попытка получить общих друзей несуществующего пользователя {}", id);
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        if (other == null) {
            log.warn("Попытка получить общих друзей несуществующего пользователя {}", otherId);
            throw new NotFoundException("Пользователь с id " + otherId + " не найден");
        }

        Set<Long> commonIds = user.getFriends().keySet().stream().filter(other.getFriends()::containsKey).collect(Collectors.toSet());

        log.info("Общие друзья {} и {}: {}", id, otherId, commonIds);

        return commonIds.stream().map(userStorage::getUser).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public List<FeedEvent> getFeedByUser(Long userId) {
        return feedStorage.getEventsByUser(userId);
    }

    private void validate(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Ошибка валидации: email пустой");
            throw new ValidationException("Email не может быть пустым");
        }
        if (!user.getEmail().contains("@")) {
            log.warn("Ошибка валидации: email {} не содержит @", user.getEmail());
            throw new ValidationException("Email должен содержать @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("Ошибка валидации: пустой login");
            throw new ValidationException("Login не может быть пустым");
        }
        if (user.getLogin().contains(" ")) {
            log.warn("Ошибка валидации: login {} содержит пробелы", user.getLogin());
            throw new ValidationException("Login не может содержать пробелы");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Ошибка валидации: дата рождения {} в будущем", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}