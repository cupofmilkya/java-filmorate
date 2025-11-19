package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.controller.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Director> getDirectors() {
        String sql = "SELECT * FROM directors";
        return jdbcTemplate.query(sql, new DirectorMapper());
    }

    @Override
    public Director getDirector(Long id) {
        String sql = "SELECT * FROM directors WHERE director_id = ?";
        List<Director> directors = jdbcTemplate.query(sql, new DirectorMapper(), id);

        if (directors.isEmpty()) {
            throw new NotFoundException("Режиссёр с id " + id + " не найден");
        }

        return directors.getFirst();
    }

    @Override
    public void addDirector(Director director) {
        String sql = "INSERT INTO directors (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        director.setId(keyHolder.getKey().longValue());
    }

    @Override
    public void updateDirector(Long id, Director director) {
        String sql = "UPDATE directors SET name = ? WHERE director_id = ?";
        int updated = jdbcTemplate.update(sql, director.getName(), id);

        if (updated == 0) {
            throw new NotFoundException("Режиссёр с id " + director.getId() + " не найден");
        }
    }

    @Override
    public void deleteDirector(Long id) {
        String sql = "DELETE FROM directors WHERE director_id = ?";
        int deleted = jdbcTemplate.update(sql, id);

        if (deleted == 0) {
            throw new NotFoundException("Режиссёр с id " + id + " не найден");
        }
    }

    @Override
    public Set<Director> getDirectorsByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) return Set.of();

        String sql = "SELECT * FROM directors WHERE director_id IN (" +
                String.join(",", ids.stream().map(String::valueOf).toList()) + ")";

        List<Director> directors = jdbcTemplate.query(sql, new DirectorMapper());
        return new HashSet<>(directors);
    }
}