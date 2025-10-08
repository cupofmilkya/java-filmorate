package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Map;

public interface UserStorage {
    void addUser(User user);

    User getUser(long id);

    Map<Long, User> getUsers();

    void updateUser(long id, User user);
}
