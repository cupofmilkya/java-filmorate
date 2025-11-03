package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.exception.FriendsAddingException;
import ru.yandex.practicum.filmorate.controller.exception.NotFoundException;
import ru.yandex.practicum.filmorate.controller.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.dto.UserDTO;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserStorage userStorage;

    public Collection<User> getUsers() {
        return userStorage.getUsers().values();
    }

    public User getUser(Long id) {
        User user = userStorage.getUser(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        return user;
    }

    public User addUser(UserDTO dto) {
        User user = convertToUser(dto);
        validate(user);

        userStorage.addUser(user);
        log.info("Создан пользователь {}", user);
        return user;
    }

    public User updateUser(Long id, UserDTO dto) {
        User user = convertToUser(dto);
        user.setId(id);
        validate(user);

        if (userStorage.getUser(id) == null) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }

        userStorage.updateUser(id, user);
        log.info("Обновлен пользователь с id={}, {}", id, user);
        return user;
    }

    public User addFriend(Long id, Long friendId) {
        User user = userStorage.getUser(id);
        User friend = userStorage.getUser(friendId);

        if (user == null) throw new NotFoundException("Пользователь с id " + id + " не найден");
        if (friend == null) throw new NotFoundException("Пользователь с id " + friendId + " не найден");
        if (id.equals(friendId)) throw new FriendsAddingException("Пользователь не может добавить себя в друзья");

        userStorage.addFriend(id, friendId);
        return userStorage.getUser(id);
    }

    public User deleteFriend(Long id, Long friendId) {
        User user = userStorage.getUser(id);
        User friend = userStorage.getUser(friendId);

        if (user == null) throw new NotFoundException("Пользователь с id " + id + " не найден");
        if (friend == null) throw new NotFoundException("Пользователь с id " + friendId + " не найден");

        userStorage.deleteFriend(id, friendId);
        return userStorage.getUser(id);
    }

    public Set<User> getFriends(Long id) {
        User user = userStorage.getUser(id);
        if (user == null) throw new NotFoundException("Пользователь с id " + id + " не найден");

        return user.getFriends().keySet().stream()
                .map(userStorage::getUser)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Set<User> getCommonFriends(Long id, Long otherId) {
        User user = userStorage.getUser(id);
        User other = userStorage.getUser(otherId);

        if (user == null) throw new NotFoundException("Пользователь с id " + id + " не найден");
        if (other == null) throw new NotFoundException("Пользователь с id " + otherId + " не найден");

        Set<Long> commonIds = user.getFriends().keySet().stream()
                .filter(other.getFriends()::containsKey)
                .collect(Collectors.toSet());

        return commonIds.stream()
                .map(userStorage::getUser)
                .collect(Collectors.toSet());
    }

    private User convertToUser(UserDTO dto) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setLogin(dto.getLogin());
        user.setName(dto.getName() == null || dto.getName().isBlank() ? dto.getLogin() : dto.getName());
        user.setBirthday(dto.getBirthday());

        if (dto.getFriendIds() != null) {
            dto.getFriendIds().forEach(id -> user.getFriends().put(id, FriendshipStatus.CONFIRMED));
        }

        return user;
    }

    private void validate(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank())
            throw new ValidationException("Email не может быть пустым");
        if (!user.getEmail().contains("@"))
            throw new ValidationException("Email должен содержать @");
        if (user.getLogin() == null || user.getLogin().isBlank())
            throw new ValidationException("Login не может быть пустым");
        if (user.getLogin().contains(" "))
            throw new ValidationException("Login не может содержать пробелы");
        if (user.getBirthday().isAfter(LocalDate.now()))
            throw new ValidationException("Дата рождения не может быть в будущем");
    }
}