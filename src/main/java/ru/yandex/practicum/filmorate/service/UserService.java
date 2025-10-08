package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.exception.FriendsAddingException;
import ru.yandex.practicum.filmorate.controller.exception.NotFoundException;
import ru.yandex.practicum.filmorate.controller.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

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
        if (user.getId() != null && userStorage.getUsers().containsKey(user.getId())) {
            log.warn("Пользователь не прошел валидацию по id (такой уже есть)");
            throw new ValidationException("Пользователь с id " + user.getId() + " уже существует");
        }

        userStorage.addUser(user);

        log.info("Создан пользователь {}", user);

        return user;
    }

    public User updateUser(User user) {
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

        if (user.getFriends().contains(friendId) || friend.getFriends().contains(id)) {
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
            throw new NotFoundException("Пользователя с айди " + id + " не найдено");
        }

        if (friend == null) {
            throw new NotFoundException("Пользователя с айди " + friendId + " не найдено");
        }

        if (!user.getFriends().contains(friendId) || !friend.getFriends().contains(id)) {
            throw new FriendsAddingException("Пользователи с айди " + id + " и " + friendId +
                    " не в друзьях друг у друга");
        }
        user.deleteFriend(friendId);
        friend.deleteFriend(id);

        log.info("Пользователь {} удалил из друзей пользователя {} ", id, friendId);

        return user;
    }

    public Set<User> getFriends(Long id) {
        User user = userStorage.getUser(id);

        if (user == null) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }

        return user.getFriends().stream()
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

        Set<Long> commonFriendIds = new HashSet<>(user.getFriends());
        commonFriendIds.retainAll(otherUser.getFriends());

        return commonFriendIds.stream()
                .map(userStorage::getUser)
                .collect(Collectors.toSet());
    }
}
