package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.config.TestConfig;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final JdbcTemplate jdbcTemplate;
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DEL ETE FROM likes");
        jdbcTemplate.update("DEL ETE FROM genre_film");
        jdbcTemplate.update("DEL ETE FROM films");
        jdbcTemplate.update("DEL ETE FROM user_friendships");
        jdbcTemplate.update("DEL ETE FROM users");
        jdbcTemplate.update("ALT ER TABLE films ALTER COLUMN film_id RESTART WITH 1");
        jdbcTemplate.update("ALT ER TABLE users ALTER COLUMN user_id RESTART WITH 1");
    }

    @Test
    @DisplayName("Добавление нового фильма")
    void testAddFilm() {
        Film film = createTestFilm("Test Film", "Test Description", MpaRating.PG13);
        filmDbStorage.addFilm(film);
        Film savedFilm = filmDbStorage.getFilm(film.getId());
        assertThat(savedFilm).isNotNull();
        assertThat(savedFilm.getId()).isEqualTo(1L);
        assertThat(savedFilm.getName()).isEqualTo("Test Film");
        assertThat(savedFilm.getDescription()).isEqualTo("Test Description");
        assertThat(savedFilm.getMpaRating()).isEqualTo(MpaRating.PG13);
        assertThat(savedFilm.getDuration()).isEqualTo(120);
        assertThat(savedFilm.getReleaseDate()).isEqualTo(LocalDate.of(2000, 1, 1));
        assertThat(savedFilm.getLikes()).isEmpty();
    }

    @Test
    @DisplayName("Добавление фильма с жанрами")
    void testAddFilmWithGenres() {
        Film film = createTestFilm("Test Film", "Test Description", MpaRating.PG13);
        Set<Genre> genres = new HashSet<>();
        genres.add(Genre.COMEDY);
        genres.add(Genre.DRAMA);
        film.setGenres(genres);
        filmDbStorage.addFilm(film);
        Film savedFilm = filmDbStorage.getFilm(film.getId());
        assertThat(savedFilm.getGenres()).hasSize(2);
        assertThat(savedFilm.getGenres()).contains(Genre.COMEDY, Genre.DRAMA);
    }

    @Test
    @DisplayName("Поиск фильма по ID")
    void testGetFilm() {
        Film film = createTestFilm("Test Film", "Test Description", MpaRating.PG13);
        filmDbStorage.addFilm(film);
        Long filmId = film.getId();
        Film foundFilm = filmDbStorage.getFilm(filmId);
        assertThat(foundFilm).isNotNull();
        assertThat(foundFilm.getId()).isEqualTo(filmId);
        assertThat(foundFilm.getName()).isEqualTo("Test Film");
    }

    @Test
    @DisplayName("Поиск несуществующего фильма")
    void testGetFilm_NotFound() {
        Film foundFilm = filmDbStorage.getFilm(999L);
        assertThat(foundFilm).isNull();
    }

    @Test
    @DisplayName("Получение всех фильмов")
    void testGetAllFilms() {
        Film film1 = createTestFilm("Film One", "Description One", MpaRating.PG);
        Film film2 = createTestFilm("Film Two", "Description Two", MpaRating.R);
        filmDbStorage.addFilm(film1);
        filmDbStorage.addFilm(film2);
        Map<Long, Film> films = filmDbStorage.getFilms();
        assertThat(films).hasSize(2);
        assertThat(films).containsKeys(1L, 2L);
        assertThat(films.get(1L).getName()).isEqualTo("Film One");
        assertThat(films.get(2L).getName()).isEqualTo("Film Two");
    }

    @Test
    @DisplayName("Добавление и удаление лайка")
    void testAddAndRemoveLike() {
        Film film = createTestFilm("Test Film", "Test Description", MpaRating.PG13);
        filmDbStorage.addFilm(film);
        Long filmId = film.getId();
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("userlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        userDbStorage.addUser(user);
        Long userId = user.getId();
        filmDbStorage.sendLike(userId, filmId);
        Set<Long> likes = filmDbStorage.getLikes(filmId);
        assertThat(likes).contains(userId);
        filmDbStorage.removeLike(userId, filmId);
        likes = filmDbStorage.getLikes(filmId);
        assertThat(likes).doesNotContain(userId);
    }

    @Test
    @DisplayName("Добавление фильма с несколькими жанрами")
    void testFilmWithMultipleGenres() {
        Film film = createTestFilm("Test Film", "Test Description", MpaRating.PG13);
        Set<Genre> genres = new HashSet<>();
        genres.add(Genre.COMEDY);
        genres.add(Genre.DRAMA);
        genres.add(Genre.ACTION);
        film.setGenres(genres);
        filmDbStorage.addFilm(film);
        Film savedFilm = filmDbStorage.getFilm(film.getId());
        assertThat(savedFilm.getGenres()).hasSize(3);
        assertThat(savedFilm.getGenres())
                .contains(Genre.COMEDY, Genre.DRAMA, Genre.ACTION);
    }

    private Film createTestFilm(String name, String description, MpaRating mpa) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpaRating(mpa);
        film.setGenres(new HashSet<>());
        return film;
    }
}