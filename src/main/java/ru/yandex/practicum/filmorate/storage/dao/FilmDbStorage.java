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
        if (directorIds == null || directorIds.isEmpty()) return;

        List<Long> ids = new ArrayList<>(directorIds);
        jdbcTemplate.batchUpdate(
                "INSERT INTO director_film (film_id, director_id) VALUES (?, ?)",
                ids,
                ids.size(),
                (ps, dId) -> {
                    ps.setLong(1, filmId);
                    ps.setLong(2, dId);
                }
        );
    }

    private void saveGenres(Long filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) return;

        List<Integer> genreIds = genres.stream()
                .map(g -> g.ordinal() + 1)
                .toList();

        jdbcTemplate.batchUpdate(
                "INSERT INTO genre_film (film_id, genre_id) VALUES (?, ?)",
                genreIds,
                genreIds.size(),
                (ps, gId) -> {
                    ps.setLong(1, filmId);
                    ps.setInt(2, gId);
                }
        );
    }

    @Override
    public Film getFilm(long id) {
        String sql = "SELECT * FROM films WHERE film_id = ?";
        List<Film> films = jdbcTemplate.query(sql, new FilmMapper(), id);
        if (films == null || films.isEmpty()) return null;

        Film film = films.get(0);
        enrichFilms(List.of(film));
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

        if (!films.isEmpty()) {
            enrichFilms(films);
        }

        return films;
    }

    @Override
    public Collection<Film> getPopularByYear(Long count, Long year) {
        String sql = """
            SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id,
                   COUNT(l.user_id) AS like_count
            FROM films f
            LEFT JOIN likes l ON f.film_id = l.film_id
            WHERE EXTRACT(YEAR FROM f.release_date) = ?
            GROUP BY f.film_id
            ORDER BY like_count DESC
            LIMIT ?
            """;

        List<Film> films = jdbcTemplate.query(sql, new FilmMapper(), year, count);

        if (!films.isEmpty()) enrichFilms(films);

        return films;
    }

    @Override
    public Collection<Film> getPopularByGenre(Long count, Long genreId) {
        String sql = """
            SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id,
                   COUNT(l.user_id) AS like_count
            FROM films f
            JOIN genre_film gf ON f.film_id = gf.film_id
            LEFT JOIN likes l ON f.film_id = l.film_id
            WHERE gf.genre_id = ?
            GROUP BY f.film_id
            ORDER BY like_count DESC
            LIMIT ?
            """;

        List<Film> films = jdbcTemplate.query(sql, new FilmMapper(), genreId, count);
        if (!films.isEmpty()) enrichFilms(films);
        return films;
    }

    @Override
    public Collection<Film> getPopularByGenreAndYear(Long count, Long genreId, Long year) {
        String sql = """
            SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id,
                   COUNT(l.user_id) AS like_count
            FROM films f
            JOIN genre_film gf ON f.film_id = gf.film_id
            LEFT JOIN likes l ON f.film_id = l.film_id
            WHERE gf.genre_id = ?
              AND EXTRACT(YEAR FROM f.release_date) = ?
            GROUP BY f.film_id
            ORDER BY like_count DESC
            LIMIT ?
            """;

        List<Film> films = jdbcTemplate.query(sql, new FilmMapper(), genreId, year, count);
        if (!films.isEmpty()) enrichFilms(films);
        return films;
    }

    @Override
    public Map<Long, Film> getFilms() {
        String sql = """
            SELECT f.film_id,
                   f.name,
                   f.description,
                   f.release_date,
                   f.duration,
                   f.mpa_id
            FROM films f
            """;

        List<Film> films = jdbcTemplate.query(sql, new FilmMapper());

        if (films.isEmpty()) return Collections.emptyMap();

        enrichFilms(films);

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

        jdbcTemplate.update("DELETE FROM director_film WHERE film_id = ?", id);
        if (film.getDirectorsId() != null && !film.getDirectorsId().isEmpty()) {
            saveDirectorLinks(id, film.getDirectorsId());
        }
    }

    @Override
    public void removeFilm(long id) {
        String sql = "DELETE FROM films WHERE film_id = ?";
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

    @Override
    public LinkedHashSet<Film> getFilmsByDirector(Long directorId) {
        String sqlFilms = """
            SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id
            FROM films f
            JOIN director_film df ON f.film_id = df.film_id
            WHERE df.director_id = ?
            """;

        List<Film> films = jdbcTemplate.query(sqlFilms, new FilmMapper(), directorId);

        if (films.isEmpty()) return new LinkedHashSet<>();

        enrichFilms(films);

        return new LinkedHashSet<>(films);
    }

    private void enrichFilms(List<Film> films) {
        if (films == null || films.isEmpty()) return;

        List<Long> filmIds = films.stream().map(Film::getId).toList();
        Map<Long, Set<Long>> likesMap = loadLikesForFilmIds(filmIds);
        Map<Long, Set<Genre>> genresMap = loadGenresForFilmIds(filmIds);
        Map<Long, Set<Long>> directorsMap = loadDirectorsForFilmIds(filmIds);

        String ids = filmIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        String sqlMpa = "SELECT film_id, mpa_id FROM films WHERE film_id IN (" + ids + ")";
        Map<Long, Integer> mpaMap = jdbcTemplate.query(sqlMpa, rs -> {
            Map<Long, Integer> map = new HashMap<>();
            while (rs.next()) {
                long filmId = rs.getLong("film_id");
                int mpaId = rs.getInt("mpa_id");
                map.put(filmId, mpaId);
            }
            return map;
        });

        for (Film f : films) {
            long id = f.getId();
            f.setLikes(likesMap.getOrDefault(id, new HashSet<>()));
            f.setGenres(genresMap.getOrDefault(id, new LinkedHashSet<>()));
            f.setDirectorsId(directorsMap.getOrDefault(id, new HashSet<>()));

            Integer mpaId = mpaMap.get(id);
            if (mpaId != null && mpaId > 0 && mpaId <= MpaRating.values().length) {
                f.setMpaRating(MpaRating.values()[mpaId - 1]);
            }
        }
    }

    private Map<Long, Set<Long>> loadLikesForFilmIds(List<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) return Collections.emptyMap();

        String ids = filmIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        String sql = "SELECT film_id, user_id FROM likes WHERE film_id IN (" + ids + ")";

        return jdbcTemplate.query(sql, rs -> {
            Map<Long, Set<Long>> map = new HashMap<>();
            while (rs.next()) {
                long filmId = rs.getLong("film_id");
                long userId = rs.getLong("user_id");
                map.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
            }
            return map;
        });
    }

    private Map<Long, Set<Genre>> loadGenresForFilmIds(List<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) return Collections.emptyMap();

        String ids = filmIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        String sql = "SELECT film_id, genre_id FROM genre_film WHERE film_id IN (" + ids + ") ORDER BY genre_id";

        return jdbcTemplate.query(sql, rs -> {
            Map<Long, Set<Genre>> map = new HashMap<>();
            while (rs.next()) {
                long filmId = rs.getLong("film_id");
                int genreId = rs.getInt("genre_id");
                if (genreId > 0 && genreId <= Genre.values().length) {
                    Genre genre = Genre.values()[genreId - 1];
                    map.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
                }
            }
            return map;
        });
    }

    private Map<Long, Set<Long>> loadDirectorsForFilmIds(List<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) return Collections.emptyMap();

        String ids = filmIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        String sql = "SELECT film_id, director_id FROM director_film WHERE film_id IN (" + ids + ")";

        return jdbcTemplate.query(sql, rs -> {
            Map<Long, Set<Long>> map = new HashMap<>();
            while (rs.next()) {
                long filmId = rs.getLong("film_id");
                long directorId = rs.getLong("director_id");
                map.computeIfAbsent(filmId, k -> new HashSet<>()).add(directorId);
            }
            return map;
        });
    }

    private void upsertGenres(long filmId, Set<Genre> genres) {
        jdbcTemplate.update("DELETE FROM genre_film WHERE film_id = ?", filmId);

        if (genres == null || genres.isEmpty()) return;

        List<Integer> ids = genres.stream()
                .filter(Objects::nonNull)
                .map(g -> g.ordinal() + 1)
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