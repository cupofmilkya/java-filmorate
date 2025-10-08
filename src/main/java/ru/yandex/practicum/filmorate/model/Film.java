package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Film.
 */
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder(toBuilder = true)
@AllArgsConstructor
public class Film {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private Set<Long> likes;

    public Film() {
        likes = new HashSet<>();
    }

    public boolean addLike(Long id) {
        return likes.add(id);
    }

    public boolean removeLike(Long id) {
        return likes.remove(id);
    }
}
