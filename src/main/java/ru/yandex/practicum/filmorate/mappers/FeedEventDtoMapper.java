package ru.yandex.practicum.filmorate.mappers;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.dto.FeedEventDTO;


@UtilityClass
public class FeedEventDtoMapper {

    public FeedEventDTO toDto(FeedEvent event) {
        if (event == null) return null;
        return FeedEventDTO.builder()
                .eventId(event.getEventId())
                .userId(event.getUserId())
                .eventType(event.getEventType())
                .operation(event.getOperation())
                .entityId(event.getEntityId())
                .timestamp(event.getCreatedAt().toEpochMilli())
                .build();
    }
}
