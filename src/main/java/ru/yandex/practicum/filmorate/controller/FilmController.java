package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.controller.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.dto.FilmDTO;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    @Autowired
    private FilmService filmService;

    @GetMapping
    public Collection<Film> getFilms() {
        return filmService.getFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        return filmService.getFilm(id);
    }

    @PostMapping
    public Film createFilm(@RequestBody FilmDTO filmDTO) {
        Film film = convertToFilm(filmDTO);
        return filmService.addFilm(film);
    }

    @PutMapping("/{id}")
    public Film updateFilm(@PathVariable Long id, @RequestBody FilmDTO filmDTO) {
        Film film = convertToFilm(filmDTO);
        film.setId(id);
        return filmService.updateFilm(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film sendLike(@PathVariable long id, @PathVariable long userId) {
        return filmService.sendLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film removeLike(@PathVariable long id, @PathVariable long userId) {
        return filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") long count) {
        return filmService.getPopularFilms(count);
    }

    private Film convertToFilm(FilmDTO dto) {
        Film film = new Film();
        film.setName(dto.getName());
        film.setDescription(dto.getDescription());
        film.setReleaseDate(dto.getReleaseDate());
        film.setDuration(dto.getDuration());

        if (dto.getGenreIds() != null && !dto.getGenreIds().isEmpty()) {
            Set<Genre> genres = dto.getGenreIds().stream()
                    .map(id -> {
                        int idx = id - 1;
                        if (idx >= 0 && idx < Genre.values().length) {
                            return Genre.values()[idx];
                        } else {
                            throw new ValidationException("Неверный ID жанра: " + id);
                        }
                    })
                    .collect(Collectors.toSet());
            film.setGenres(genres);
        }

        if (dto.getGenreIds() != null && !dto.getGenreIds().isEmpty()) {
            Set<Genre> genres = dto.getGenreIds().stream()
                    .map(id -> {
                        int idx = id - 1;
                        if (idx >= 0 && idx < Genre.values().length) {
                            return Genre.values()[idx];
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            film.setGenres(genres);
        }

        return film;
    }
}