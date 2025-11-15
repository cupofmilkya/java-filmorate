package ru.yandex.practicum.filmorate.model.dto;

import lombok.*;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeedEventDTO {
    private Long eventId;
    private Long userId;
    private EventType eventType;
    private Operation operation;
    private Long entityId;
    private Long timestamp;
}
