package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    @Test
    @DisplayName("Проверка на создание фильма с валидными данными")
    void createValidFilm() {
        FilmController controller = new FilmController();
        Film film = Film.builder()
                .name("Valid Film")
                .description("Описание фильма")
                .releaseDate(new Date())
                .duration(Duration.ofMinutes(120))
                .build();

        Film created = controller.createFilm(film);
        assertNotNull(created);
        assertEquals("Valid Film", created.getName());
    }

    @Test
    @DisplayName("Проверка на пустое имя фильма")
    void createFilmEmptyName() {
        FilmController controller = new FilmController();
        Film film = Film.builder()
                .name(" ")
                .description("Описание")
                .releaseDate(new Date())
                .duration(Duration.ofMinutes(100))
                .build();

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createFilm(film));
        assertEquals("Пустое значение имени", ex.getMessage());
    }

    @Test
    @DisplayName("Проверка на слишком длинное описание фильма")
    void createFilmTooLongDescription() {
        FilmController controller = new FilmController();
        String longDesc = "a".repeat(201);
        Film film = Film.builder()
                .name("Film")
                .description(longDesc)
                .releaseDate(new Date())
                .duration(Duration.ofMinutes(100))
                .build();

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createFilm(film));
        assertEquals("Максимальная длина описания — 200 символов", ex.getMessage());
    }

    @Test
    @DisplayName("Проверка на дату релиза до 1895-12-28")
    void createFilmBeforeBarrierDate() {
        FilmController controller = new FilmController();
        Film film = Film.builder()
                .name("Film")
                .description("Описание")
                .releaseDate(Date.from(
                        LocalDate.of(1800, 1, 1)
                                .atStartOfDay(ZoneId.systemDefault())
                                .toInstant()
                ))
                .duration(Duration.ofMinutes(100))
                .build();

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createFilm(film));
        assertTrue(ex.getMessage().contains("Дата релиза не может быть раньше"));
    }

    @Test
    @DisplayName("Проверка на отрицательную продолжительность фильма")
    void createFilmNegativeDuration() {
        FilmController controller = new FilmController();
        Film film = Film.builder()
                .name("Film")
                .description("Описание")
                .releaseDate(new Date())
                .duration(Duration.ofMinutes(-50))
                .build();

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createFilm(film));
        assertEquals("Продолжительность фильма должна быть положительным числом", ex.getMessage());
    }
}