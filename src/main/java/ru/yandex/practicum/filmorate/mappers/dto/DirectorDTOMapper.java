package ru.yandex.practicum.filmorate.mappers.dto;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.dto.DirectorDTO;

public final class DirectorDTOMapper {

    public static DirectorDTO convertToDto(Director director) {
        return DirectorDTO.builder()
                .id(director.getId())
                .name(director.getName())
                .build();
    }

    public static Director convertToDirector(DirectorDTO dto) {
        return Director.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }
}
