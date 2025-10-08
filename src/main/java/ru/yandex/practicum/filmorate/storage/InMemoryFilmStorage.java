package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films;

    public InMemoryFilmStorage() {
        films = new HashMap<>();
    }

    @Override
    public void addFilm(Film film) {
        if (film.getId() == null) {
            film.setId(getNextId());
        }

        films.put(film.getId(), film);
    }

    @Override
    public Film getFilm(long id) {
        return films.get(id);
    }

    @Override
    public Map<Long, Film> getFilms() {
        return films;
    }

    @Override
    public void updateFilm(long id, Film film) {
        Film filmToUpdate = films.get(id);
        if (filmToUpdate != null) {
            filmToUpdate.setName(film.getName());
            filmToUpdate.setDescription(film.getDescription());
            filmToUpdate.setReleaseDate(film.getReleaseDate());
            filmToUpdate.setDuration(film.getDuration());
            filmToUpdate.setLikes(film.getLikes());
        }
    }

    private long getNextId() {
        return films.keySet().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0) + 1;
    }
}