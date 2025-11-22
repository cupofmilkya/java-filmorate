package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public interface FilmStorage {
    void addFilm(Film film);

    Film getFilm(long id);

    Map<Long, Film> getFilms();

    void updateFilm(long id, Film film);

    boolean addLike(long filmId, long userId);

    boolean removeLike(long filmId, long userId);

    void removeFilm(long id);

    LinkedHashSet<Film> getFilmsByDirector(Long directorId);

    Collection<Film> getPopularByYear(Long count, Long year);

    Collection<Film> getPopularByGenre(Long count, Long genreId);

    Collection<Film> getPopularByGenreAndYear(Long count, Long genreId, Long year);

    List<Film> searchFilms(String query, String by);
}