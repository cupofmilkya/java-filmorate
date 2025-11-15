package ru.yandex.practicum.filmorate.model.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.Set;
import jakarta.validation.constraints.*;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class FilmDTO {

    private Long id;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не может превышать 200 символов")
    private String description;

    @NotNull(message = "Дата релиза обязательна")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность должна быть положительной")
    private int duration;

    private Set<GenreDTO> genres;

    @NotNull(message = "MPA рейтинг обязателен")
    private MpaDTO mpa;

    private Set<DirectorDTO> directors;
}