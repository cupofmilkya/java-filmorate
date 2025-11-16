package ru.yandex.practicum.filmorate.model;

import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Director {
    private Long id;
    private String name;
}