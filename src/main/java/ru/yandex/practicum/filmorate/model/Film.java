package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Film.
 */
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder(toBuilder = true)
public class Film {
    private Long id;
    private String name;
    private String description;
    private Date releaseDate;
    private int duration;
}
