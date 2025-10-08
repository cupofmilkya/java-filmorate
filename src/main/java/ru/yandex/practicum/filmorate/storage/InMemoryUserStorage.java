package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users;

    public InMemoryUserStorage() {
        users = new HashMap<>();
    }

    @Override
    public void addUser(User user) {
        if (user.getId() == null) {
            user.setId(getNextId());
        }

        users.put(user.getId(), user);
    }

    @Override
    public User getUser(long Id) {
        return users.get(Id);
    }

    @Override
    public Map<Long, User> getUsers() {
        return users;
    }

    @Override
    public void updateUser(long Id, User user) {
        User userToUpdate = users.get(Id);
        if (userToUpdate != null) {
            userToUpdate.setName(user.getName());
            userToUpdate.setBirthday(user.getBirthday());
            userToUpdate.setLogin(user.getLogin());
            userToUpdate.setEmail(user.getEmail());
            userToUpdate.setFriends(user.getFriends());
        }
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}