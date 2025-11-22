package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.mappers.dto.DirectorDTOMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.dto.DirectorDTO;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorStorage directorStorage;

    public List<DirectorDTO> getDirectorsDto() {
        return getDirectors().stream()
                .map(DirectorDTOMapper::convertToDto)
                .toList();
    }

    public DirectorDTO getDirectorDto(Long id) {
        return DirectorDTOMapper.convertToDto(getDirector(id));
    }

    public DirectorDTO addDirectorDto(DirectorDTO directorDto) {
        Director director = DirectorDTOMapper.convertToDirector(directorDto);
        return DirectorDTOMapper.convertToDto(addDirector(director));
    }

    public DirectorDTO updateDirectorDto(DirectorDTO directorDto) {
        Director director = DirectorDTOMapper.convertToDirector(directorDto);
        return DirectorDTOMapper.convertToDto(updateDirector(director));
    }

    public DirectorDTO deleteDirectorDto(Long id) {
        return DirectorDTOMapper.convertToDto(deleteDirector(id));
    }

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