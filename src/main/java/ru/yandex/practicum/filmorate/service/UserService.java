package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.exception.FriendsAddingException;
import ru.yandex.practicum.filmorate.controller.exception.NotFoundException;
import ru.yandex.practicum.filmorate.controller.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    @Autowired
    UserStorage userStorage;

    public Collection<User> getUsers() {
        return userStorage.getUsers().values();
    }

    public User addUser(User user) {
        validate(user);

        if (user.getId() != null && userStorage.getUsers().containsKey(user.getId())) {
            log.warn("Пользователь не прошел валидацию по id (такой уже есть)");
            throw new ValidationException("Пользователь с id " + user.getId() + " уже существует");
        }

        userStorage.addUser(user);

        log.info("Создан пользователь {}", user);

        return user;
    }

    public User updateUser(User user) {
        validate(user);

        if (user.getId() != null && !userStorage.getUsers().containsKey(user.getId())) {
            log.warn("Пользователь не прошел валидацию по id (такого нет)");
            throw new NotFoundException("Пользователя с id " + user.getId() + " нет");
        }

        User updatedUser = user.toBuilder().build();
        userStorage.updateUser(user.getId(), updatedUser);

        log.info("Обновлен пользователь с id={}, {}", user.getId(), user);

        return user;
    }

    public User addFriend(long id, long friendId) {
        User user = userStorage.getUser(id);
        User friend = userStorage.getUser(friendId);

        if (user == null) {
            throw new NotFoundException("Пользователя с айди " + id + " не найдено");
        }

        if (friend == null) {
            throw new NotFoundException("Пользователя с айди " + friendId + " не найдено");
        }

        if (user.getFriends().containsKey(friendId) || friend.getFriends().containsKey(id)) {
            throw new FriendsAddingException("Пользователи с айди " + id + " и " + friendId +
                    " уже в друзьях друг у друга");
        }
        user.addFriend(friendId);
        friend.addFriend(id);

        log.info("Пользователь {} добавил в друзья пользователя {} ", id, friendId);

        return user;
    }

    public User deleteFriend(long id, long friendId) {
        User user = userStorage.getUser(id);
        User friend = userStorage.getUser(friendId);

        if (user == null) {
            throw new NotFoundException("Пользователя с id " + id + " не найдено");
        }
        if (friend == null) {
            throw new NotFoundException("Пользователя с id " + friendId + " не найдено");
        }

        // Если не друзья — просто ничего не делаем
        if (!user.getFriends().containsKey(friendId) || !friend.getFriends().containsKey(id)) {
            log.info("Попытка удалить несуществующую дружбу между {} и {}", id, friendId);
            return user; // ← просто вернуть 200 OK без исключений
        }

        user.deleteFriend(friendId);
        friend.deleteFriend(id);

        log.info("Пользователь {} удалил из друзей пользователя {}", id, friendId);
        return user;
    }

    public Set<User> getFriends(Long id) {
        User user = userStorage.getUser(id);

        if (user == null) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }

        return user.getFriends().keySet().stream()
                .map(userStorage::getUser)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Set<User> getCommonFriends(long id, long otherId) {
        User user = userStorage.getUser(id);
        User otherUser = userStorage.getUser(otherId);

        if (user == null) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }

        if (otherUser == null) {
            throw new NotFoundException("Пользователь с id " + otherId + " не найден");
        }

        Set<Long> commonFriendIds = new HashSet<>(user.getFriends().keySet());
        commonFriendIds.retainAll(otherUser.getFriends().keySet());

        return commonFriendIds.stream()
                .map(userStorage::getUser)
                .collect(Collectors.toSet());
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
