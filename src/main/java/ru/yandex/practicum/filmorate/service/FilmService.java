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

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
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

    public Film addFilm(Film film) {
        if (film.getId() != null && filmStorage.getFilms().containsKey(film.getId())) {
            log.warn("Фильм не прошёл валидацию по id (такой уже есть)");
            throw new ValidationException("Фильм с id " + film.getId() + " уже существует");
        }

        filmStorage.addFilm(film);

        log.info("Создан фильм {} ", film);
        return film;
    }

    public Film updateFilm(Film film) {
        if (film.getId() != null && !filmStorage.getFilms().containsKey(film.getId())) {
            log.warn("Фильм не прошёл валидацию по id (такого нет)");
            throw new NotFoundException("Фильма с id " + film.getId() + " нет");
        }

        Film updatedFilm = film.toBuilder().build();
        filmStorage.updateFilm(film.getId(), updatedFilm);

        log.info("Обновлен фильм с id={}, {}", film.getId(), film);
        return film;
    }

    public Film sendLike(long id, long userId) {
        Film film = filmStorage.getFilm(id);
        User user = userStorage.getUser(userId);

        if (film == null) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        if (film.getLikes().contains(userId)) {
            throw new LikesSendingException("Пользователи с айди " + userId + " уже добавил лайк посту с айди  "
                    + id);
        }

        film.addLike(userId);

        log.info("Пользователь {} поставил лайк фильму {} ", userId, id);

        return film;
    }

    public Film removeLike(long id, long userId) {
        Film film = filmStorage.getFilm(id);
        User user = userStorage.getUser(userId);

        if (film == null) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        if (!film.getLikes().contains(userId)) {
            throw new LikesSendingException("Пользователи с айди " + userId + " не добавлял лайк посту с айди  "
                    + id);
        }

        film.removeLike(userId);

        log.info("Пользователь {} убрал лайк у фильма {} ", userId, id);

        return film;
    }

    public List<Film> getPopularFilms(long count) {
        return filmStorage.getFilms().values().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }
}