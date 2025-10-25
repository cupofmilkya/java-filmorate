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
    private Set<Genre> genres;
    private MpaRating mpaRating;

    public Film() {
        likes = new HashSet<>();
        genres = new HashSet<>();
    }

    public void addLike(Long id) {
        likes.add(id);
    }

    public void removeLike(Long id) {
        likes.remove(id);
    }

    public boolean addGenre(Genre genre) {
        return genres.add(genre);
    }

    public boolean removeGenre(Genre genre) {
        return genres.remove(genre);
    }
}
