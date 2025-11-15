package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.controller.exception.NotFoundException;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
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
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, film.getReleaseDate() != null ? java.sql.Date.valueOf(film.getReleaseDate()) : null);
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpaRating().ordinal() + 1);
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveGenres(film.getId(), film.getGenres());
        }
    }

    private void saveGenres(Long filmId, Set<Genre> genres) {
        String sql = "INSERT INTO genre_film (film_id, genre_id) VALUES (?, ?)";
        for (Genre genre : genres) {
            jdbcTemplate.update(sql, filmId, genre.ordinal() + 1);
        }
    }

    @Override
    public Film getFilm(long id) {
        String sql = "SELECT * FROM films WHERE film_id = ?";
        List<Film> films = jdbcTemplate.query(sql, new FilmMapper(), id);
        if (films.isEmpty()) return null;

        Film film = films.getFirst();
        film.setLikes(getLikes(film.getId()));

        String mpaSql = "SELECT mpa_id FROM films WHERE film_id = ?";
        Integer mpaId = jdbcTemplate.queryForObject(mpaSql, Integer.class, film.getId());
        if (mpaId != null && mpaId > 0 && mpaId <= MpaRating.values().length) {
            film.setMpaRating(MpaRating.values()[mpaId - 1]);
        }

        String genresSql = "SELECT genre_id FROM genre_film WHERE film_id = ?";
        List<Integer> genreIds = jdbcTemplate.queryForList(genresSql, Integer.class, film.getId());
        if (genreIds != null && !genreIds.isEmpty()) {
            Set<Genre> genres = genreIds.stream()
                    .filter(Objects::nonNull)
                    .map(idVal -> {
                        int idx = idVal - 1;
                        if (idx >= 0 && idx < Genre.values().length) return Genre.values()[idx];
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            film.setGenres(genres);
        } else {
            film.setGenres(Set.of());
        }

        return film;
    }

    @Override
    public Collection<Film> getPopularFilms(Long count, Long genreId, Long year) {
        if ((genreId != null) && (year != null)) {
            return getPopularByGenreAndYear(count, genreId, year);
        } else if (genreId == null) {
            return getPopularByYear(count, year);
        } else {
            return getPopularByGenre(count, genreId);
        }
    }

    private Collection<Film> getPopularByYear(Long count, Long year) {
        String sql = "SELECT f.*, count(*) likkes FROM films f " +
                "INNER JOIN likes l ON l.film_id = f.film_id " +
                "WHERE  " +
                "EXTRACT(YEAR FROM CAST(f.release_date AS date)) = ? " +
                "GROUP BY f.film_id " +
                "ORDER BY likkes DESC " +
                "LIMIT ?";
        System.out.println("getPopularByYear");

        List<Film> films = jdbcTemplate.query(sql, new FilmMapper(),
                year,
                count);
        return films;
    }

    private Collection<Film> getPopularByGenre(Long count, Long genreId) {
        String sql = "SELECT f.*, count(*) likkes FROM films f " +
                "INNER JOIN likes l ON l.film_id = f.film_id " +
                "INNER JOIN genre_film fg ON f.film_id = fg.film_id " +
                "WHERE fg.genre_id = ? " +
                "GROUP BY f.film_id " +
                "ORDER BY likkes DESC " +
                "LIMIT ?";
        System.out.println("getPopularByGenre");

        List<Film> films = jdbcTemplate.query(sql, new FilmMapper(),
                genreId,
                count);
        return films;
    }

    private Collection<Film> getPopularByGenreAndYear(Long count, Long genreId, Long year) {
        String sql = "SELECT f.*, count(*) likkes FROM films f " +
                "INNER JOIN likes l ON l.film_id = f.film_id " +
                "INNER JOIN genre_film fg ON f.film_id = fg.film_id " +
                "WHERE fg.genre_id = ? " +
                "AND EXTRACT(YEAR FROM CAST(f.release_date AS date)) = ? " +
                "GROUP BY f.film_id " +
                "ORDER BY likkes DESC " +
                "LIMIT ?";
        System.out.println("getPopularByGenre");

        List<Film> films = jdbcTemplate.query(sql, new FilmMapper(),
                genreId,
                year,
                count);
        return films;
    }

    @Override
    public Map<Long, Film> getFilms() {
        String sql = "SELECT * FROM films";
        List<Film> films = jdbcTemplate.query(sql, new FilmMapper());

        for (Film f : films) {
            f.setLikes(getLikes(f.getId()));

            // mpa
            Integer mpaId = jdbcTemplate.queryForObject(
                    "SELECT mpa_id FROM films WHERE film_id = ?", Integer.class, f.getId());
            if (mpaId != null && mpaId > 0 && mpaId <= MpaRating.values().length) {
                f.setMpaRating(MpaRating.values()[mpaId - 1]);
            }

            // genres
            List<Integer> genreIds = jdbcTemplate.queryForList(
                    "SELECT genre_id FROM genre_film WHERE film_id = ?", Integer.class, f.getId());
            if (genreIds != null && !genreIds.isEmpty()) {
                Set<Genre> genres = genreIds.stream()
                        .map(idVal -> Genre.values()[idVal - 1])
                        .collect(Collectors.toSet());
                f.setGenres(genres);
            } else {
                f.setGenres(Set.of());
            }
        }
        return films.stream().collect(Collectors.toMap(Film::getId, f -> f));
    }

    @Override
    public void updateFilm(long id, Film film) {
        String sql = "UPDATE films SET name=?, description=?, release_date=?, duration=?, mpa_id=? WHERE film_id=?";
        int updated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpaRating() != null ? film.getMpaRating().ordinal() + 1 : null,
                id
        );
        if (updated == 0) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
    }

    public void sendLike(Long userId, Long filmId) {
        String sql = "INSERT INTO likes (user_id, film_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, filmId);
    }

    public void removeLike(Long userId, Long filmId) {
        String sql = "DELETE FROM likes WHERE user_id = ? AND film_id = ?";
        jdbcTemplate.update(sql, userId, filmId);
    }

    public Set<Long> getLikes(Long filmId) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, filmId));
    }

    private boolean hasLike(Long filmId, Long userId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, filmId, userId) > 0;
    }
}