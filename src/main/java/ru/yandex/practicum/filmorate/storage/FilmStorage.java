package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Map;

public interface FilmStorage {
    void addFilm(Film film);

    Film getFilm(long Id);

    Map<Long, Film> getFilms();

    void updateFilm(long Id, Film film);
}
