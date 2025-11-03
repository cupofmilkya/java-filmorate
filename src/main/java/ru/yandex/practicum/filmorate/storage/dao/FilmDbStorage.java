package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@Primary
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addFilm(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpaRating() != null ? film.getMpaRating().ordinal() + 1 : null
        );
    }

    @Override
    public Film getFilm(long id) {
        String sql = "SELECT * FROM films WHERE film_id = ?";
        List<Film> films = jdbcTemplate.query(sql, new FilmMapper(), id);
        if (films.isEmpty()) {
            return null;
        }
        Film film = films.getFirst();
        film.setLikes(getLikes(film.getId()));
        return film;
    }

    @Override
    public Map<Long, Film> getFilms() {
        String sql = "SELECT * FROM films";
        List<Film> films = jdbcTemplate.query(sql, new FilmMapper());

        films.forEach(f -> f.setLikes(getLikes(f.getId())));

        return films.stream().collect(Collectors.toMap(Film::getId, f -> f));
    }

    @Override
    public void updateFilm(long id, Film film) {
        String sql = "UPDATE films SET name=?, description=?, release_date=?, duration=? WHERE film_id=?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                id);
    }

    public void sendLike(Long userId, Long filmId) {
        String sql = "INSERT INTO likes (user_id, film_id) VALUES (?, ?)";
        jdbcTemplate.update(sql,
                userId,
                filmId);
    }

    public void removeLike(Long userId, Long filmId) {
        String sql = "DELETE FROM likes WHERE user_id = ? AND film_id = ?";
        jdbcTemplate.update(sql, userId, filmId);
    }

    private Set<Long> getLikes(Long filmId) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, filmId));
    }

    private boolean hasLike(Long filmId, Long userId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, filmId, userId) > 0;
    }
}