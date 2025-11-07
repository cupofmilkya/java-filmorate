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
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorage;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final JdbcTemplate jdbcTemplate;
    private final UserDbStorage userDbStorage;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM user_friendships");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1");
    }

    @Test
    @DisplayName("Добавление нового пользователя")
    void testAddUser() {
        User user = createTestUser("test@mail.ru", "testlogin", "Test User");
        userDbStorage.addUser(user);
        User savedUser = userDbStorage.getUser(user.getId());
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isEqualTo(1L);
        assertThat(savedUser.getEmail()).isEqualTo("test@mail.ru");
        assertThat(savedUser.getLogin()).isEqualTo("testlogin");
        assertThat(savedUser.getName()).isEqualTo("Test User");
        assertThat(savedUser.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    @DisplayName("Поиск пользователя по ID")
    void testGetUser() {
        User user = createTestUser("test@mail.ru", "testlogin", "Test User");
        userDbStorage.addUser(user);
        Long userId = user.getId();
        User foundUser = userDbStorage.getUser(userId);
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(userId);
        assertThat(foundUser.getEmail()).isEqualTo("test@mail.ru");
        assertThat(foundUser.getFriends()).isEmpty();
    }

    @Test
    @DisplayName("Поиск несуществующего пользователя")
    void testGetUser_NotFound() {
        User foundUser = userDbStorage.getUser(999L);
        assertThat(foundUser).isNull();
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void testGetAllUsers() {
        User user1 = createTestUser("test1@mail.ru", "login1", "User One");
        User user2 = createTestUser("test2@mail.ru", "login2", "User Two");
        userDbStorage.addUser(user1);
        userDbStorage.addUser(user2);
        Map<Long, User> users = userDbStorage.getUsers();
        assertThat(users).hasSize(2);
        assertThat(users).containsKeys(1L, 2L);
        assertThat(users.get(1L).getEmail()).isEqualTo("test1@mail.ru");
        assertThat(users.get(2L).getEmail()).isEqualTo("test2@mail.ru");
    }

    @Test
    @DisplayName("Обновление данных пользователя")
    void testUpdateUser() {
        User user = createTestUser("test@mail.ru", "testlogin", "Test User");
        userDbStorage.addUser(user);
        Long userId = user.getId();
        user.setName("Updated Name");
        user.setEmail("updated@mail.ru");
        user.setLogin("updatedlogin");
        userDbStorage.updateUser(userId, user);
        User updatedUser = userDbStorage.getUser(userId);
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@mail.ru");
        assertThat(updatedUser.getLogin()).isEqualTo("updatedlogin");
    }

    @Test
    @DisplayName("Добавление и удаление друга")
    void testAddAndDeleteFriend() {
        User user1 = createTestUser("user1@mail.ru", "login1", "User One");
        User user2 = createTestUser("user2@mail.ru", "login2", "User Two");
        userDbStorage.addUser(user1);
        userDbStorage.addUser(user2);
        Long user1Id = user1.getId();
        Long user2Id = user2.getId();
        userDbStorage.addFriend(user1Id, user2Id);
        User userWithFriend = userDbStorage.getUser(user1Id);
        assertThat(userWithFriend.getFriends()).containsKey(user2Id);
        userDbStorage.deleteFriend(user1Id, user2Id);
        User userWithoutFriend = userDbStorage.getUser(user1Id);
        assertThat(userWithoutFriend.getFriends()).doesNotContainKey(user2Id);
    }

    private User createTestUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }
}