package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Set;

public interface DirectorStorage {
    List<Director> getDirectors();

    Director getDirector(Long id);

    void addDirector(Director director);

    void updateDirector(Long id, Director director);

    void deleteDirector(Long id);

    Set<Director> getDirectorsByIds(Set<Long> ids);
}