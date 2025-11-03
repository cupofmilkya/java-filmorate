package ru.yandex.practicum.filmorate.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.storage.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorage;

import javax.sql.DataSource;

@TestConfiguration
public class TestConfig {

    @Bean
    public UserDbStorage userDbStorage(DataSource dataSource) {
        return new UserDbStorage(new JdbcTemplate(dataSource));
    }

    @Bean
    public FilmDbStorage filmDbStorage(DataSource dataSource) {
        return new FilmDbStorage(new JdbcTemplate(dataSource));
    }
}