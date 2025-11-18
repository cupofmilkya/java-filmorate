package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.Operation;

import java.util.List;

public interface FeedStorage {

    void saveEvent(Long userId, EventType type, Operation op, Long entityId);

    List<FeedEvent> getEventsByUser(Long userId);
}
