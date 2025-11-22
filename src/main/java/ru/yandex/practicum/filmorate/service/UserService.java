package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.exception.FriendsAddingException;
import ru.yandex.practicum.filmorate.controller.exception.NotFoundException;
import ru.yandex.practicum.filmorate.controller.exception.ValidationException;
import ru.yandex.practicum.filmorate.mappers.FeedEventDtoMapper;
import ru.yandex.practicum.filmorate.mappers.dto.UserDTOMapper;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.model.dto.FeedEventDTO;
import ru.yandex.practicum.filmorate.model.dto.UserDTO;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;
    private final FeedStorage feedStorage;

    public Collection<User> getUsers() {
        return userStorage.getUsers().values();
    }

    public List<UserDTO> getUsersDto() {
        return getUsers().stream()
                .map(UserDTOMapper::convertToDto)
                .sorted((u1, u2) -> Long.compare(u1.getId(), u2.getId()))
                .collect(Collectors.toList());
    }

    public UserDTO getUserDtoById(Long id) {
        User user = getUser(id);
        return UserDTOMapper.convertToDto(user);
    }

    public UserDTO addUserDto(UserDTO userDTO) {
        User user = UserDTOMapper.convertToUser(userDTO);
        addUser(user);
        return UserDTOMapper.convertToDto(user);
    }

    public UserDTO updateUserDto(UserDTO userDTO) {
        User user = UserDTOMapper.convertToUser(userDTO);
        updateUser(user);
        return UserDTOMapper.convertToDto(user);
    }

    public UserDTO addFriendDto(Long userId, Long friendId) {
        User user = addFriend(userId, friendId);
        return UserDTOMapper.convertToDto(user);
    }

    public UserDTO deleteFriendDto(Long userId, Long friendId) {
        User user = deleteFriend(userId, friendId);
        return UserDTOMapper.convertToDto(user);
    }

    public List<UserDTO> getFriendsDto(Long userId) {
        return getFriends(userId).stream()
                .map(UserDTOMapper::convertToDto)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getCommonFriendsDto(Long userId, Long otherId) {
        return getCommonFriends(userId, otherId).stream()
                .map(UserDTOMapper::convertToDto)
                .collect(Collectors.toList());
    }

    public List<FeedEventDTO> getFeedByUserDto(Long userId) {
        requireUserExists(userId);
        return getFeedByUser(userId).stream()
                .map(FeedEventDtoMapper::toDto)
                .collect(Collectors.toList());
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
        normalizeUserName(user);
        userStorage.addUser(user);
        log.info("Создан пользователь {}", user);
        return user;
    }

    public User updateUser(User user) {
        if (user.getId() == null) {
            log.warn("Обновление пользователя без указания id");
            throw new ValidationException("ID не указан");
        }

        if (userStorage.getUser(user.getId()) == null) {
            log.warn("Не найден пользователь для обновления с id {}", user.getId());
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }

        normalizeUserName(user);
        userStorage.updateUser(user.getId(), user);
        log.info("Обновлен пользователь с id={}, {}", user.getId(), user);
        return user;
    }

    public void removeUser(long id) {
        if (userStorage.getUser(id) == null) {
            log.warn("Попытка удалить пользователя: не найден пользователь c id = {}", id);
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        userStorage.removeUser(id);
        log.info("Пользователь с id = {} удален", id);
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

    public List<User> getFriends(Long id) {
        User user = userStorage.getUser(id);

        if (user == null) {
            log.warn("Попытка получить список друзей несуществующего пользователя {}", id);
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }

        return userStorage.getFriends(id);
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
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

        Set<Long> commonIds = user.getFriends().keySet().stream()
                .filter(other.getFriends()::containsKey)
                .collect(Collectors.toSet());

        log.info("Общие друзья {} и {}: {}", id, otherId, commonIds);

        return userStorage.getCommonFriends(id, otherId);
    }

    public void requireUserExists(long userId) {
        if (userStorage.getUser(userId) == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
    }

    public List<FeedEvent> getFeedByUser(Long userId) {
        return feedStorage.getEventsByUser(userId);
    }

    private void normalizeUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}