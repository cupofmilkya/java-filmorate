package ru.yandex.practicum.filmorate.model.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
public class FilmDTO {
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private Set<Integer> genreIds;
    private MpaDTO mpa;
}