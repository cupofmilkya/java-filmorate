package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorStorage directorStorage;

    public List<Director> getDirectors() {
        return directorStorage.getDirectors();
    }

    public Director getDirector(Long id) {
        return directorStorage.getDirector(id);
    }

    public Director addDirector(Director director) {
        directorStorage.addDirector(director);
        log.info("Добавлен новый режиссёр: {}", director);
        return director;
    }

    public Director updateDirector(Director director) {
        directorStorage.updateDirector(director.getId(), director);
        log.info("Обновлён режиссёр с id {}: {}", director.getId(), director);
        return director;
    }

    public Director deleteDirector(Long id) {
        Director director = directorStorage.getDirector(id);
        directorStorage.deleteDirector(id);
        log.info("Удалён режиссёр с id {}", id);
        return director;
    }
}