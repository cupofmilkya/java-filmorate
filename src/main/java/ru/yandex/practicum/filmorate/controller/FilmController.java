package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.controller.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

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

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        validate(film);
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        validate(film);
        return filmService.updateFilm(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film sendLike(
            @PathVariable long id,
            @PathVariable long userId
    ) {
        return filmService.sendLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film removeLike(
            @PathVariable long id,
            @PathVariable long userId
    ) {
        return filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(
            @RequestParam(defaultValue = "10") long count
    ) {
        return filmService.getPopularFilms(count);
    }

    private void validate(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Фильм не прошёл валидацию по имени");
            throw new ValidationException("Пустое значение имени");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.warn("Фильм не прошёл валидацию по длине описания");
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }

        LocalDate barrier = LocalDate.of(1895, 12, 28);
        LocalDate releaseDate = film.getReleaseDate();

        if (releaseDate.isBefore(barrier)) {
            log.warn("Фильм не прошёл валидацию по дате релиза");
            throw new ValidationException("Дата релиза не может быть раньше " + barrier);
        }

        if (film.getDuration() <= 0) {
            log.warn("Фильм не прошёл валидацию по продолжительности");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}