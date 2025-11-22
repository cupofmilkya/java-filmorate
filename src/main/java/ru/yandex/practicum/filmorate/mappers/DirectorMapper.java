package ru.yandex.practicum.filmorate.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DirectorMapper implements RowMapper<Director> {
    @Override
    public Director mapRow(ResultSet rs, int rowNum) throws SQLException {
        Long id = rs.getLong("director_id");
        String name = rs.getString("name");

        return Director.builder()
                .id(id)
                .name(name)
                .build();
    }
}
