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
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpaRating().ordinal() + 1);
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            upsertGenres(film.getId(), film.getGenres());
        }

        if (film.getDirectorsId() != null && !film.getDirectorsId().isEmpty()) {
            saveDirectorLinks(film.getId(), film.getDirectorsId());
        }
    }

    private void saveDirectorLinks(Long filmId, Set<Long> directorIds) {
        String sql = "INSERT INTO director_film (film_id, director_id) VALUES (?, ?)";
        for (Long directorId : directorIds) {
            jdbcTemplate.update(sql, filmId, directorId);
        }
    }

    private void saveGenres(Long filmId, Set<Genre> genres) {
        String sql = "INSERT INTO genre_film (film_id, genre_id) VALUES (?, ?)";
        for (Genre genre : genres) {
            jdbcTemplate.update(sql, filmId, genre.ordinal() + 1);
        }
    }

    private void saveDirectors(Long filmId, Set<Long> directorIds) {
        String sql = "INSERT INTO director_film (film_id, director_id) VALUES (?, ?)";
        for (Long directorId : directorIds) {
            System.out.println("=== INSERTING DIRECTOR " + directorId + " FOR FILM " + filmId + " ===");
            jdbcTemplate.update(sql, filmId, directorId);
        }
    }

    @Override
    public Film getFilm(long id) {
        String sql = "SELECT * FROM films WHERE film_id = ?";
        List<Film> films = jdbcTemplate.query(sql, new FilmMapper(), id);
        if (films.isEmpty()) return null;

        Film film = films.getFirst();
        film.setLikes(getLikes(film.getId()));

        List<Integer> mpaIds = jdbcTemplate.queryForList(
                "SELECT mpa_id FROM films WHERE film_id = ?",
                Integer.class, film.getId()
        );
        if (!mpaIds.isEmpty()) {
            Integer mpaId = mpaIds.get(0);
            if (mpaId != null && mpaId > 0 && mpaId <= MpaRating.values().length) {
                film.setMpaRating(MpaRating.values()[mpaId - 1]);
            }
        }

        List<Integer> genreIds = jdbcTemplate.queryForList(
                "SELECT genre_id FROM genre_film WHERE film_id = ? ORDER BY genre_id",
                Integer.class, film.getId()
        );
        if (genreIds != null && !genreIds.isEmpty()) {
            Set<Genre> genres = genreIds.stream()
                    .filter(Objects::nonNull)
                    .map(idVal -> {
                        int idx = idVal - 1;
                        return (idx >= 0 && idx < Genre.values().length) ? Genre.values()[idx] : null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            film.setGenres(genres);
        } else {
            film.setGenres(Set.of());
        }

        List<Long> directorIds = jdbcTemplate.queryForList(
                "SELECT director_id FROM director_film WHERE film_id = ?",
                Long.class, film.getId()
        );
        film.setDirectorsId(new HashSet<>(directorIds));

        return film;
    }

    public List<Film> searchFilms(String query, String by) {
        StringBuilder sql = new StringBuilder(
                "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, " +
                "COUNT(DISTINCT l.user_id) as like_count " +
                "FROM films f " + "LEFT JOIN director_film df ON f.film_id = df.film_id " +
                "LEFT JOIN directors d ON df.director_id = d.director_id " +
                "LEFT JOIN likes l ON f.film_id = l.film_id " + "WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();
        String searchPattern = "%" + query.toLowerCase() + "%";

        if (by.contains("title") && by.contains("director")) {
            sql.append(" AND (LOWER(f.name) LIKE ? OR LOWER(d.name) LIKE ?)");
            params.add(searchPattern);
            params.add(searchPattern);
        } else if (by.contains("title")) {
            sql.append(" AND LOWER(f.name) LIKE ?");
            params.add(searchPattern);
        } else if (by.contains("director")) {
            sql.append(" AND LOWER(d.name) LIKE ?");
            params.add(searchPattern);
        }

        sql.append(" GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id");
        sql.append(" ORDER BY COUNT(l.user_id) DESC");

        List<Film> films = jdbcTemplate.query(sql.toString(), new FilmMapper(), params.toArray());

        for (Film film : films) {
            film.setLikes(getLikes(film.getId()));
            loadMpaRating(film);
            loadGenres(film);
            loadDirectorsId(film);
        }

        return films;
    }

    @Override
    public Collection<Film> getPopularByYear(Long count, Long year) {
        String sql = "SELECT f.*, count(*) likkes FROM films f " +
                "INNER JOIN likes l ON l.film_id = f.film_id " +
                "WHERE  " +
                "EXTRACT(YEAR FROM CAST(f.release_date AS date)) = ? " +
                "GROUP BY f.film_id " +
                "ORDER BY likkes DESC " +
                "LIMIT ?";

        List<Film> films = jdbcTemplate.query(sql, new FilmMapper(), year, count);

        films.forEach(film -> {
            film.setLikes(getLikes(film.getId()));
            loadMpaRating(film);
            loadGenres(film);
            loadDirectorsId(film);
        });

        return films;
    }

    @Override
    public Collection<Film> getPopularByGenre(Long count, Long genreId) {
        String sql = "SELECT f.*, count(*) likkes FROM films f " +
                "INNER JOIN likes l ON l.film_id = f.film_id " +
                "INNER JOIN genre_film fg ON f.film_id = fg.film_id " +
                "WHERE fg.genre_id = ? " +
                "GROUP BY f.film_id " +
                "ORDER BY likkes DESC " +
                "LIMIT ?";

        List<Film> films = jdbcTemplate.query(sql, new FilmMapper(), genreId, count);

        films.forEach(film -> {
            film.setLikes(getLikes(film.getId()));
            loadMpaRating(film);
            loadGenres(film);
            loadDirectorsId(film);
        });

        return films;
    }

    @Override
    public Collection<Film> getPopularByGenreAndYear(Long count, Long genreId, Long year) {
        String sql = "SELECT f.*, count(*) likkes FROM films f " +
                "INNER JOIN likes l ON l.film_id = f.film_id " +
                "INNER JOIN genre_film fg ON f.film_id = fg.film_id " +
                "WHERE fg.genre_id = ? " +
                "AND EXTRACT(YEAR FROM CAST(f.release_date AS date)) = ? " +
                "GROUP BY f.film_id " +
                "ORDER BY likkes DESC " +
                "LIMIT ?";

        List<Film> films = jdbcTemplate.query(sql, new FilmMapper(), genreId, year, count);

        films.forEach(film -> {
            film.setLikes(getLikes(film.getId()));
            loadMpaRating(film);
            loadGenres(film);
            loadDirectorsId(film);
        });

        return films;
    }

    @Override
    public Map<Long, Film> getFilms() {
        String sql = "SELECT * FROM films";
        List<Film> films = jdbcTemplate.query(sql, new FilmMapper());

        for (Film f : films) {
            f.setLikes(getLikes(f.getId()));

            List<Integer> mpaIds = jdbcTemplate.queryForList(
                    "SELECT mpa_id FROM films WHERE film_id = ?",
                    Integer.class, f.getId()
            );
            if (!mpaIds.isEmpty()) {
                Integer mpaId = mpaIds.get(0);
                if (mpaId != null && mpaId > 0 && mpaId <= MpaRating.values().length) {
                    f.setMpaRating(MpaRating.values()[mpaId - 1]);
                }
            }

            List<Integer> genreIds = jdbcTemplate.queryForList(
                    "SELECT genre_id FROM genre_film WHERE film_id = ? ORDER BY genre_id",
                    Integer.class, f.getId()
            );
            if (genreIds != null && !genreIds.isEmpty()) {
                Set<Genre> genres = genreIds.stream()
                        .filter(Objects::nonNull)
                        .map(idVal -> {
                            int idx = idVal - 1;
                            return (idx >= 0 && idx < Genre.values().length) ? Genre.values()[idx] : null;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                f.setGenres(genres);
            } else {
                f.setGenres(Set.of());
            }

            List<Long> directorIds = jdbcTemplate.queryForList(
                    "SELECT director_id FROM director_film WHERE film_id = ?",
                    Long.class, f.getId()
            );
            f.setDirectorsId(directorIds != null ? new HashSet<>(directorIds) : new HashSet<>());
        }

        return films.stream().collect(Collectors.toMap(Film::getId, film -> film));
    }

    @Override
    public void updateFilm(long id, Film film) {
        jdbcTemplate.update(
                "UPDATE films SET name=?, description=?, release_date=?, duration=?, mpa_id=? WHERE film_id=?",
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpaRating() != null ? film.getMpaRating().ordinal() + 1 : null,
                id
        );

        upsertGenres(id, film.getGenres());

        jdbcTemplate.update("DELETE FROM genre_film WHERE film_id = ?", id);
        if (film.getGenres() != null) {
            saveGenres(id, film.getGenres());
        }

        jdbcTemplate.update("DELETE FROM director_film WHERE film_id = ?", id);
        if (film.getDirectorsId() != null) {
            saveDirectorLinks(id, film.getDirectorsId());
        }
    }

    @Override
    public void removeFilm(long id) {
        String sql = "DELETE FROM films " + "WHERE film_id = ?";
        jdbcTemplate.update(sql, id);
    }

    public boolean addLike(long filmId, long userId) {
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?",
                Integer.class, filmId, userId
        );
        if (exists != null && exists > 0) {
            return false;
        }
        jdbcTemplate.update("INSERT INTO likes (film_id, user_id) VALUES (?, ?)", filmId, userId);
        return true;
    }

    public boolean removeLike(long filmId, long userId) {
        int rows = jdbcTemplate.update(
                "DELETE FROM likes WHERE film_id = ? AND user_id = ?",
                filmId, userId
        );
        return rows > 0;
    }

    public Set<Long> getLikes(Long filmId) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, filmId));
    }

    private boolean hasLike(Long filmId, Long userId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        Integer cnt = jdbcTemplate.queryForObject(sql, Integer.class, filmId, userId);
        return cnt != null && cnt > 0;
    }

    public LinkedHashSet<Film> getFilmsByDirector(Long directorId) {
        String sql =
                "SELECT f.* " +
                        "FROM films AS f " +
                        "JOIN director_film AS df ON f.film_id = df.film_id " +
                        "WHERE df.director_id = ?";

        List<Film> films = jdbcTemplate.query(sql, new FilmMapper(), directorId);

        for (Film film : films) {
            film.setLikes(getLikes(film.getId()));
            List<Integer> mpaIds = jdbcTemplate.queryForList(
                    "SELECT mpa_id FROM films WHERE film_id = ?",
                    Integer.class, film.getId()
            );
            if (!mpaIds.isEmpty()) {
                Integer mpaId = mpaIds.get(0);
                if (mpaId != null && mpaId > 0 && mpaId <= MpaRating.values().length) {
                    film.setMpaRating(MpaRating.values()[mpaId - 1]);
                }
            }

            List<Integer> genreIds = jdbcTemplate.queryForList(
                    "SELECT genre_id FROM genre_film WHERE film_id = ? ORDER BY genre_id",
                    Integer.class, film.getId()
            );
            if (genreIds != null && !genreIds.isEmpty()) {
                Set<Genre> genres = genreIds.stream()
                        .filter(Objects::nonNull)
                        .map(idVal -> {
                            int idx = idVal - 1;
                            return (idx >= 0 && idx < Genre.values().length) ? Genre.values()[idx] : null;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                film.setGenres(genres);
            } else {
                film.setGenres(Set.of());
            }

            List<Long> directorIds = jdbcTemplate.queryForList(
                    "SELECT director_id FROM director_film WHERE film_id = ?",
                    Long.class, film.getId()
            );
            film.setDirectorsId(directorIds != null ? new HashSet<>(directorIds) : new HashSet<>());
        }
        return new LinkedHashSet<>(films);
    }

    private void loadDirectorsId(Film film) {
        String sql = "SELECT director_id FROM director_film WHERE film_id = ?";
        List<Long> directorIds = jdbcTemplate.queryForList(sql, Long.class, film.getId());
        film.setDirectorsId(directorIds != null ? new HashSet<>(directorIds) : new HashSet<>());
    }

    private void loadMpaRating(Film film) {
        String mpaSql = "SELECT mpa_id FROM films WHERE film_id = ?";
        Integer mpaId = jdbcTemplate.queryForObject(mpaSql, Integer.class, film.getId());
        if (mpaId != null && mpaId > 0 && mpaId <= MpaRating.values().length) {
            film.setMpaRating(MpaRating.values()[mpaId - 1]);
        }
    }

    private void loadGenres(Film film) {
        String sql = "SELECT genre_id FROM genre_film WHERE film_id = ? ORDER BY genre_id";
        List<Integer> genreIds = jdbcTemplate.queryForList(sql, Integer.class, film.getId());

        LinkedHashSet<Genre> genres = genreIds.stream()
                .filter(Objects::nonNull)
                .map(id -> Genre.values()[id - 1])
                .collect(Collectors.toCollection(LinkedHashSet::new));

        film.setGenres(genres);
    }

    private void upsertGenres(long filmId, Set<Genre> genres) {
        // Сначала удаляем старые связи
        jdbcTemplate.update("DELETE FROM genre_film WHERE film_id = ?", filmId);

        if (genres == null || genres.isEmpty()) return;

        // Делаем distinct + сортировка по id и батчим вставку
        List<Integer> ids = genres.stream()
                .filter(Objects::nonNull)
                .map(g -> g.ordinal() + 1) // enum -> id (1..6)
                .distinct()
                .sorted()
                .toList();

        jdbcTemplate.batchUpdate(
                "INSERT INTO genre_film (film_id, genre_id) VALUES (?, ?)",
                ids,
                ids.size(),
                (ps, genreId) -> {
                    ps.setLong(1, filmId);
                    ps.setInt(2, genreId);
                }
        );
    }
}