package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.controller.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.FeedEventDtoMapper;
import ru.yandex.practicum.filmorate.mappers.dto.FilmDTOMapper;
import ru.yandex.practicum.filmorate.mappers.dto.UserDTOMapper;
import ru.yandex.practicum.filmorate.model.dto.FeedEventDTO;
import ru.yandex.practicum.filmorate.model.dto.FilmDTO;
import ru.yandex.practicum.filmorate.model.dto.UserDTO;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FilmService filmService;

    @GetMapping
    public ResponseEntity<Collection<UserDTO>> getUsers() {
        Collection<UserDTO> users = userService.getUsers().stream()
                .map(UserDTOMapper::convertToDto)
                .sorted(Comparator.comparingLong(UserDTO::getId))
                .toList();

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO userdto = UserDTOMapper.convertToDto(userService.getUser(id));

        return ResponseEntity.ok(userdto);
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO user) {
        UserDTO userdto = UserDTOMapper.convertToDto(userService.addUser(UserDTOMapper.convertToUser(user)));

        return ResponseEntity.status(HttpStatus.CREATED).body(userdto);
    }

    @PutMapping
    public ResponseEntity<UserDTO> updateUser(@Valid @RequestBody UserDTO user) {
        UserDTO userdto = UserDTOMapper.convertToDto(userService.updateUser(UserDTOMapper.convertToUser(user)));

        return ResponseEntity.ok(userdto);
    }

    @DeleteMapping("/{id}")
    public void removeUser(@PathVariable long id) {
        userService.removeUser(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<UserDTO> addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        UserDTO userdto = UserDTOMapper.convertToDto(userService.addFriend(id, friendId));

        return ResponseEntity.ok(userdto);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<UserDTO> deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        UserDTO userdto = UserDTOMapper.convertToDto(userService.deleteFriend(id, friendId));

        return ResponseEntity.ok(userdto);
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<List<UserDTO>> getFriends(@PathVariable Long id) {
        List<UserDTO> users = userService.getFriends(id).stream()
                .map(UserDTOMapper::convertToDto)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<List<UserDTO>> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        List<UserDTO> users = userService.getCommonFriends(id, otherId).stream()
                .map(UserDTOMapper::convertToDto)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}/feed")
    public ResponseEntity<List<FeedEventDTO>> getFeeds(@PathVariable Long id) {

        List<FeedEventDTO> body = userService.getFeedByUser(id).stream()
                .map(FeedEventDtoMapper::toDto)
                .toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}/recommendations")
    public ResponseEntity<List<FilmDTO>> getRecommendations(@PathVariable Long id) {
        List<FilmDTO> recommendedFilms = filmService.getRecommendations(id).stream()
                .map(FilmDTOMapper::convertToDto)
                .toList();
        return ResponseEntity.ok(recommendedFilms);
    }
}