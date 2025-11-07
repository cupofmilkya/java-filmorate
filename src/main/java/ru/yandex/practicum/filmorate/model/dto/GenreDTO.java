package ru.yandex.practicum.filmorate.model.dto;

import lombok.*;
import ru.yandex.practicum.filmorate.model.Genre;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GenreDTO {
    private int id;
    private String name;

    public static GenreDTO fromEnum(Genre g) {
        return new GenreDTO(g.ordinal() + 1, switch (g) {
            case COMEDY -> "Комедия";
            case DRAMA -> "Драма";
            case CARTOON -> "Мультфильм";
            case THRILLER -> "Триллер";
            case DOCUMENTARY -> "Документальный";
            case ACTION -> "Боевик";
        });
    }
}