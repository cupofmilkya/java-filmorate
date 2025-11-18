package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@EqualsAndHashCode(of = "eventId")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeedEvent {
    private Long eventId;
    private Long userId;
    private EventType eventType;
    private Operation operation;
    private Long entityId;
    private Instant createdAt;
}


