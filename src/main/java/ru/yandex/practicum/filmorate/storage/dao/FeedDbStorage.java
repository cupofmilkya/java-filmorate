package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mappers.FeedEventRowMapper;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.FeedStorage;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FeedDbStorage implements FeedStorage {

    private final JdbcTemplate jdbc;

    public static final String SQL_GET_EVENTS_BY_USER = """
            SELECT event_id, user_id, event_type, operation, entity_id, created_at
            FROM user_feed
            WHERE user_id = ?
            ORDER BY event_id ASC
            """;

    @Override
    public void saveEvent(Long userId, EventType type, Operation op, Long entityId) {
        String sql = """
            INSERT INTO user_feed (user_id, event_type, operation, entity_id)
            VALUES (?,?,?,?)
            """;
        jdbc.update(sql, userId, type.name(), op.name(), entityId);
    }

    @Override
    public List<FeedEvent> getEventsByUser(Long userId) {
        return jdbc.query(SQL_GET_EVENTS_BY_USER, new FeedEventRowMapper(), userId);
    }
}
