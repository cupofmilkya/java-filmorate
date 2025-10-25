package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder(toBuilder = true)
@AllArgsConstructor
public class User {
    private Long id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    private Map<Long, FriendshipStatus> friends;

    public User() {
        friends = new HashMap<>();
    }

    public void addFriend(Long id) {
        friends.put(id, FriendshipStatus.CONFIRMED);
    }

    public void deleteFriend(Long id) {
        friends.remove(id);
    }
}