package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.controller.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.dto.GenreDTO;
import ru.yandex.practicum.filmorate.model.dto.MpaDTO;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping
public class ReferenceDataController {

    @GetMapping("/genres")
    public List<GenreDTO> getAllGenres() {
        return Arrays.stream(Genre.values())
                .map(GenreDTO::fromEnum)
                .collect(Collectors.toList());
    }

    @GetMapping("/genres/{id}")
    public GenreDTO getGenreById(@PathVariable int id) {
        if (id < 1 || id > Genre.values().length) {
            throw new NotFoundException("Жанр с id " + id + " не найден");
        }
        return GenreDTO.fromEnum(Genre.values()[id - 1]);
    }

    @GetMapping("/mpa")
    public List<MpaDTO> getAllMpa() {
        return Arrays.stream(MpaRating.values())
                .map(MpaDTO::fromEnum)
                .collect(Collectors.toList());
    }

    @GetMapping("/mpa/{id}")
    public MpaDTO getMpaById(@PathVariable int id) {
        if (id < 1 || id > MpaRating.values().length) {
            throw new NotFoundException("MPA с id " + id + " не найден");
        }
        return MpaDTO.fromEnum(MpaRating.values()[id - 1]);
    }
}