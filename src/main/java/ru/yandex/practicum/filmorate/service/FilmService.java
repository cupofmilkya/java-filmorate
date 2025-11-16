package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.exception.LikesSendingException;
import ru.yandex.practicum.filmorate.controller.exception.NotFoundException;
import ru.yandex.practicum.filmorate.controller.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final FeedStorage feedStorage;

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
        if (film.getId() != null && filmStorage.getFilms().containsKey(film.getId())) {
            log.warn("Фильм не прошёл валидацию по id (такой уже есть)");
            throw new ValidationException("Фильм с id " + film.getId() + " уже существует");
        }

        validateReleaseDate(film);

        filmStorage.addFilm(film);

        log.info("Создан фильм {} ", film);
        return film;
    }

    public Film updateFilm(Film film) {
        if (filmStorage.getFilm(film.getId()) == null) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }

        validateReleaseDate(film);

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
        feedStorage.saveEvent(userId, EventType.LIKE, Operation.ADD, id);
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
        feedStorage.saveEvent(userId, EventType.LIKE, Operation.REMOVE, id);
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

    private void validateReleaseDate(Film film) {
        LocalDate barrier = LocalDate.of(1895, 12, 28);

        if (film.getReleaseDate() == null) {
            throw new ValidationException("Дата релиза не указана");
        }

        if (film.getReleaseDate().isBefore(barrier)) {
            throw new ValidationException("Дата релиза не может быть раньше " + barrier);
        }
    }
}