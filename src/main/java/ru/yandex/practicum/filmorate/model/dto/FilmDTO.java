package ru.yandex.practicum.filmorate.model.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class FilmDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private Set<GenreDTO> genres;
    private MpaDTO mpa;
    private Set<DirectorDTO> directors;
}