package ru.yandex.practicum.filmorate.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;

public class FilmMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Long id = rs.getLong("film_id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        LocalDate releaseDate = rs.getDate("release_date") != null
                ? rs.getDate("release_date").toLocalDate()
                : null;
        int duration = rs.getInt("duration");

        MpaRating mpa = null;
        int mpaId = rs.getInt("mpa_id");
        if (mpaId > 0 && mpaId <= MpaRating.values().length) {
            mpa = MpaRating.values()[mpaId - 1];
        }

        return Film.builder()
                .id(id)
                .name(name)
                .description(description)
                .releaseDate(releaseDate)
                .duration(duration)
                .mpaRating(mpa)
                .likes(new HashSet<>())
                .genres(new HashSet<>())
                .directorsId(new HashSet<>())
                .build();
    }
}