package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.List;

@Service
@Slf4j
public class DirectorService {

    @Autowired
    DirectorStorage directorStorage;

    public List<Director> getDirectors() {
        return directorStorage.getDirectors();
    }

    public Director getDirector(Long id) {
        return directorStorage.getDirector(id);
    }

    public Director addDirector(Director director) {
        validate(director);
        directorStorage.addDirector(director);
        log.info("Добавлен новый режиссёр: {}", director);
        return director;
    }

    public Director updateDirector(Director director) {
        validate(director);
        directorStorage.updateDirector(director.getId(), director);
        log.info("Обновлён режиссёр с id {}: {}", director.getId(), director);
        return director;
    }

    public Director deleteDirector(Long id) {
        Director director = directorStorage.getDirector(id);
        validate(director);
        directorStorage.deleteDirector(id);
        log.info("Удалён режиссёр с id {}", id);
        return director;
    }

    private void validate(Director director) {
        if (director == null) {
            log.warn("Ошибка валидации: объект Director равен null");
            throw new ValidationException("Объект режиссёра не может быть пустым");
        }

        if (director.getName() == null || director.getName().isBlank()) {
            log.warn("Ошибка валидации: имя режиссёра пустое");
            throw new ValidationException("Имя режиссёра не может быть пустым");
        }

        if (director.getName().length() > 100) {
            log.warn("Ошибка валидации: имя режиссёра слишком длинное ({} символов)", director.getName().length());
            throw new ValidationException("Имя режиссёра не может быть длиннее 100 символов");
        }
    }
}