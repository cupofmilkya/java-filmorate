package ru.yandex.practicum.filmorate.mappers.dto;

import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.dto.UserDTO;

import java.util.HashSet;
import java.util.LinkedHashSet;

public final class UserDTOMapper {

    public static UserDTO convertToDto(User user) {
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

    public static User convertToUser(UserDTO dto) {
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
