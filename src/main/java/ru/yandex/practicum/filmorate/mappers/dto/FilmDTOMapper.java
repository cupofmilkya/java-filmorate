package ru.yandex.practicum.filmorate.mappers.dto;

import ru.yandex.practicum.filmorate.controller.exception.NotFoundException;
import ru.yandex.practicum.filmorate.controller.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.dto.DirectorDTO;
import ru.yandex.practicum.filmorate.model.dto.FilmDTO;
import ru.yandex.practicum.filmorate.model.dto.GenreDTO;
import ru.yandex.practicum.filmorate.model.dto.MpaDTO;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class FilmDTOMapper {

    public static FilmDTO convertToDto(Film film) {
        return FilmDTO.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpa(film.getMpaRating() != null ? MpaDTO.fromEnum(film.getMpaRating()) : null)
                .genres(film.getGenres() != null && !film.getGenres().isEmpty()
                        ? film.getGenres().stream()
                        .map(GenreDTO::fromEnum)
                        .sorted(Comparator.comparingInt(GenreDTO::getId))
                        .collect(Collectors.toCollection(LinkedHashSet::new))
                        : new LinkedHashSet<>())
                .directors(film.getDirectorsId() != null && !film.getDirectorsId().isEmpty()
                        ? film.getDirectorsId().stream()
                        .map(id -> new DirectorDTO(id, null))
                        .collect(Collectors.toCollection(LinkedHashSet::new))
                        : new LinkedHashSet<>())
                .build();
    }

    public static Film convertToFilm(FilmDTO dto) {
        Film film = new Film();
        film.setId(dto.getId());
        film.setName(dto.getName());
        film.setDescription(dto.getDescription());
        film.setReleaseDate(dto.getReleaseDate());
        film.setDuration(dto.getDuration());

        if (dto.getMpa() != null) {
            int mpaId = dto.getMpa().getId();
            if (mpaId > 0 && mpaId <= MpaRating.values().length) {
                film.setMpaRating(MpaRating.values()[mpaId - 1]);
            } else {
                throw new NotFoundException("Не существует MPA с ID: " + mpaId);
            }
        } else {
            throw new ValidationException("Не указан ID MPA");
        }

        if (dto.getGenres() != null && !dto.getGenres().isEmpty()) {
            Set<Genre> genres = dto.getGenres().stream()
                    .map(g -> {
                        int index = g.getId() - 1;
                        if (index < 0 || index >= Genre.values().length) {
                            throw new NotFoundException("Не существует жанра с ID: " + g.getId());
                        }
                        return Genre.values()[index];
                    })
                    .collect(Collectors.toSet());
            film.setGenres(genres);
        } else {
            film.setGenres(Set.of());
        }

        if (dto.getDirectors() != null && !dto.getDirectors().isEmpty()) {
            Set<Long> directorIds = dto.getDirectors().stream()
                    .map(DirectorDTO::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            film.setDirectorsId(directorIds);
        } else {
            film.setDirectorsId(Set.of());
        }

        return film;
    }
}
