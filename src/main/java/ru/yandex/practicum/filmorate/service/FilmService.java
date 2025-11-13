package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.exception.LikesSendingException;
import ru.yandex.practicum.filmorate.controller.exception.NotFoundException;
import ru.yandex.practicum.filmorate.controller.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {

    @Autowired
    private FilmStorage filmStorage;
    @Autowired
    private UserStorage userStorage;

    public Collection<Film> getFilms() {
        return filmStorage.getFilms().values();
    }

    public Film getFilm(Long id) {
        Film film = filmStorage.getFilm(id);
        if (film == null) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
        return film;
    }

    public Film addFilm(Film film) {
        validate(film);
        if (film.getId() != null && filmStorage.getFilms().containsKey(film.getId())) {
            log.warn("Фильм не прошёл валидацию по id (такой уже есть)");
            throw new ValidationException("Фильм с id " + film.getId() + " уже существует");
        }

        filmStorage.addFilm(film);

        log.info("Создан фильм {} ", film);
        return film;
    }

    public Film updateFilm(Film film) {
        validate(film);

        if (filmStorage.getFilm(film.getId()) == null) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }

        filmStorage.updateFilm(film.getId(), film);
        log.info("Обновлен фильм с id={}, {}", film.getId(), film);
        return film;
    }

    public Film sendLike(long id, long userId) {
        Film film = filmStorage.getFilm(id);
        User user = userStorage.getUser(userId);

        if (film == null) throw new NotFoundException("Фильм с id " + id + " не найден");
        if (user == null) throw new NotFoundException("Пользователь с id " + userId + " не найден");

        if (film.getLikes().contains(userId)) {
            throw new LikesSendingException("Пользователь с id " + userId + " уже добавил лайк фильму с id " + id);
        }

        filmStorage.sendLike(userId, id);

        film.addLike(userId);

        log.info("Пользователь {} поставил лайк фильму {} ", userId, id);
        return film;
    }

    public Film removeLike(long id, long userId) {
        Film film = filmStorage.getFilm(id);
        User user = userStorage.getUser(userId);

        if (film == null) throw new NotFoundException("Фильм с id " + id + " не найден");
        if (user == null) throw new NotFoundException("Пользователь с id " + userId + " не найден");

        if (!film.getLikes().contains(userId)) {
            throw new LikesSendingException("Пользователь с id " + userId + " не добавлял лайк фильму с id " + id);
        }

        filmStorage.removeLike(userId, id);
        film.removeLike(userId);

        log.info("Пользователь {} убрал лайк у фильма {} ", userId, id);
        return film;
    }

    public Collection<Film> getPopularFilms(int count) {
        return filmStorage.getFilms().values().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .toList();
    }

    public LinkedHashSet<Film> getFilmsByDirector(Long directorId, String sortBy) {
        LinkedHashSet<Film> films = filmStorage.getFilmsByDirector(directorId);

        if (films.isEmpty()) {
            throw new NotFoundException("У режиссёра с id=" + directorId + " нет фильмов");
        }

        return switch (sortBy.toLowerCase()) {
            case "year" -> films.stream()
                    .sorted(Comparator.comparing(
                            (Film f) -> f.getReleaseDate() != null ? f.getReleaseDate() : LocalDate.MIN,
                            Comparator.naturalOrder()
                    ))
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            case "likes" -> films.stream()
                    .sorted(Comparator.comparingInt((Film f) -> f.getLikes() != null ? f.getLikes().size() : 0)
                            .reversed())
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            default -> throw new ValidationException("Некорректный параметр sortBy: " + sortBy);
        };
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