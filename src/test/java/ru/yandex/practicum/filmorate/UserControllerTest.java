package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.controller.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UserControllerTest {

    @Test
    @DisplayName("Проверка на пустую электронную почту")
    void createUserEmptyEmail() {
        UserController controller = new UserController();

        User user = User.builder()
                .email("")
                .login("validLogin")
                .name("Name")
                .birthday(Date.from(LocalDate.of(2000, 1, 1)
                        .atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .build();

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createUser(user));
        assertEquals("Электронная почта не может быть пустой", ex.getMessage());
    }

    @Test
    @DisplayName("Проверка на некорректную электронную почту")
    void createUserInvalidEmail() {
        UserController controller = new UserController();

        User user = User.builder()
                .email("invalidEmail")
                .login("validLogin")
                .name("Name")
                .birthday(Date.from(LocalDate.of(2000, 1, 1)
                        .atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .build();

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createUser(user));
        assertEquals("Электронная почта должна содержать символ '@'", ex.getMessage());
    }

    @Test
    @DisplayName("Проверка на пустой логин")
    void createUserEmptyLogin() {
        UserController controller = new UserController();

        User user = User.builder()
                .email("user@example.com")
                .login("")
                .name("Name")
                .birthday(Date.from(LocalDate.of(2000, 1, 1)
                        .atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .build();

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createUser(user));
        assertEquals("Логин не может быть пустым", ex.getMessage());
    }

    @Test
    @DisplayName("Проверка на логин с пробелами")
    void createUserLoginWithSpaces() {
        UserController controller = new UserController();

        User user = User.builder()
                .email("user@example.com")
                .login("invalid login")
                .name("Name")
                .birthday(Date.from(LocalDate.of(2000, 1, 1)
                        .atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .build();

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createUser(user));
        assertEquals("Логин не может содержать пробелы", ex.getMessage());
    }

    @Test
    @DisplayName("Проверка подстановки логина вместо пустого имени")
    void createUserEmptyName() {
        UserController controller = new UserController();

        User user = User.builder()
                .email("user@example.com")
                .login("login123")
                .name("")
                .birthday(Date.from(LocalDate.of(2000, 1, 1)
                        .atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .build();

        User created = controller.createUser(user);
        assertEquals("login123", created.getName());
    }

    @Test
    @DisplayName("Проверка даты рождения в будущем")
    void createUserFutureBirthday() {
        UserController controller = new UserController();

        User user = User.builder()
                .email("user@example.com")
                .login("validLogin")
                .name("Name")
                .birthday(Date.from(LocalDate.now().plusDays(1)
                        .atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .build();

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createUser(user));
        assertEquals("Дата рождения не может быть в будущем", ex.getMessage());
    }
}