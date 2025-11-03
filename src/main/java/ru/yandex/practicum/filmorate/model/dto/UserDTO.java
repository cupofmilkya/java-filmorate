package ru.yandex.practicum.filmorate.model.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    private Set<Long> friendIds;
}