package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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
    private Set<Long> friends;

    public User() {
        friends = new HashSet<>();
    }

    public boolean addFriend(Long id) {
        return friends.add(id);
    }

    public boolean deleteFriend(Long id) {
        return friends.remove(id);
    }
}