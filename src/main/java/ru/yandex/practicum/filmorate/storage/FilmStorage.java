package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public interface FilmStorage {
    void addFilm(Film film);

    Film getFilm(long id);

    Map<Long, Film> getFilms();

    void updateFilm(long id, Film film);

    void sendLike(Long userId, Long filmId);

    void removeLike(Long userId, Long filmId);

    void removeFilm(long id);

    LinkedHashSet<Film> getFilmsByDirector(Long directorId);

    List<Film> searchFilms(String query, String by);
}