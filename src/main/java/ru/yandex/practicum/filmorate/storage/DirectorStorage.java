package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorStorage {
    List<Director> getDirectors();

    Director getDirector(Long id);

    void addDirector(Director director);

    void updateDirector(Long id, Director director);

    void deleteDirector(Long id);
}