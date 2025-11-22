package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Map;

public interface UserStorage {
    void addUser(User user);

    User getUser(long id);

    Map<Long, User> getUsers();

    void updateUser(long id, User user);

    void deleteFriend(Long id, Long friendId);

    void removeUser(long id);

    void addFriend(Long id, Long friendId);

    List<User> getFriends(Long userId);

    List<User> getCommonFriends(Long userId, Long otherId);
}
