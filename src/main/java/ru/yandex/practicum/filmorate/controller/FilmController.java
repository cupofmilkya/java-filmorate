package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.controller.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getFilms() {
        return films.values();
    }

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        validateFilm(film);
        if (film.getId() != null && films.containsKey(film.getId())) {
            log.warn("Фильм не прошел валидацию по id (такой уже есть)");
            throw new ValidationException("Фильм с id " + film.getId() + " уже есть");
        }

        if (film.getId() == null) {
            film.setId(getNextId());
        }

        films.put(film.getId(), film);

        log.info("Создан фильм {}", film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        validateFilm(film);

        if (film.getId() != null && !films.containsKey(film.getId())) {
            log.warn("Фильм не прошел валидацию по id (такого нет)");
            throw new ValidationException("Фильма с id " + film.getId() + " нет");
        }

        Film updatedFilm = film.toBuilder().build();
        films.put(film.getId(), updatedFilm);

        log.info("Обновлен фильм с id={}, {}", film.getId(), film);
        return updatedFilm;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Фильм не прошел валидацию по имени");
            throw new ValidationException("Пустое значение имени");
        }

        if (film.getDescription().length() > 200) {
            log.warn("Фильм не прошел валидацию по длине описания");
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }

        LocalDate barrier = LocalDate.of(1895, 12, 28);
        LocalDate releaseDate = film.getReleaseDate();

        if (releaseDate.isBefore(barrier)) {
            log.warn("Фильм не прошел валидацию по дате");
            throw new ValidationException("Дата релиза не может быть раньше " + barrier);
        }

        if (film.getDuration() <= 0) {
            log.warn("Фильм не прошел валидацию по продолжительности");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}