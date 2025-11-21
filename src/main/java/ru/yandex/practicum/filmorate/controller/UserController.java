package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.dto.FeedEventDTO;
import ru.yandex.practicum.filmorate.model.dto.FilmDTO;
import ru.yandex.practicum.filmorate.model.dto.UserDTO;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FilmService filmService;

    @GetMapping
    public ResponseEntity<Collection<UserDTO>> getUsers() {
        return ResponseEntity.ok(userService.getUsersDto());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserDtoById(id));
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.addUserDto(user));
    }

    @PutMapping
    public ResponseEntity<UserDTO> updateUser(@Valid @RequestBody UserDTO user) {
        return ResponseEntity.ok(userService.updateUserDto(user));
    }

    @DeleteMapping("/{id}")
    public void removeUser(@PathVariable long id) {
        userService.removeUser(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<UserDTO> addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        return ResponseEntity.ok(userService.addFriendDto(id, friendId));
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<UserDTO> deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        return ResponseEntity.ok(userService.deleteFriendDto(id, friendId));
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<List<UserDTO>> getFriends(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getFriendsDto(id));
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<List<UserDTO>> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        return ResponseEntity.ok(userService.getCommonFriendsDto(id, otherId));
    }

    @GetMapping("/{id}/feed")
    public ResponseEntity<List<FeedEventDTO>> getFeeds(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getFeedByUserDto(id));
    }

    @GetMapping("/{id}/recommendations")
    public ResponseEntity<List<FilmDTO>> getRecommendations(@PathVariable Long id) {
        return ResponseEntity.ok(filmService.getRecommendationsDto(id));
    }
}