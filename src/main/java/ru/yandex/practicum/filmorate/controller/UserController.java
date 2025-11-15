package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.dto.UserDTO;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<Collection<UserDTO>> getUsers() {
        Collection<UserDTO> users = userService.getUsers().stream()
                .map(this::convertToDto)
                .sorted(Comparator.comparingLong(UserDTO::getId))
                .toList();

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO userdto = convertToDto(userService.getUser(id));

        return ResponseEntity.ok(userdto);
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO user) {
        UserDTO userdto = convertToDto(userService.addUser(convertToUser(user)));

        return ResponseEntity.status(HttpStatus.CREATED).body(userdto);
    }

    @PutMapping
    public ResponseEntity<UserDTO> updateUser(@RequestBody UserDTO user) {
        UserDTO userdto = convertToDto(userService.updateUser(convertToUser(user)));

        return ResponseEntity.ok(userdto);
    }

    @DeleteMapping("/{id}")
    public void removeUser(@PathVariable long id) {
        userService.removeUser(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<UserDTO> addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        UserDTO userdto = convertToDto(userService.addFriend(id, friendId));

        return ResponseEntity.ok(userdto);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<UserDTO> deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        UserDTO userdto = convertToDto(userService.deleteFriend(id, friendId));

        return ResponseEntity.ok(userdto);
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<Set<UserDTO>> getFriends(@PathVariable Long id) {
        Set<UserDTO> users = userService.getFriends(id).stream()
                .map(this::convertToDto)
                .collect(Collectors.toSet());

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Set<UserDTO> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        return userService.getCommonFriends(id, otherId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toSet());
    }

    private UserDTO convertToDto(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .login(user.getLogin())
                .name(user.getName())
                .birthday(user.getBirthday())
                .friendIds(user.getFriends() != null && !user.getFriends().isEmpty()
                    ? new LinkedHashSet<>(user.getFriends().keySet()) : new HashSet<>())
                .build();
    }

    private User convertToUser(UserDTO dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setEmail(dto.getEmail());
        user.setLogin(dto.getLogin());
        user.setName(dto.getName());
        user.setBirthday(dto.getBirthday());

        if (dto.getFriendIds() != null && !dto.getFriendIds().isEmpty()) {
            dto.getFriendIds().forEach(friendId ->
                    user.getFriends().put(friendId, FriendshipStatus.CONFIRMED));
        }

        return user;
    }
}