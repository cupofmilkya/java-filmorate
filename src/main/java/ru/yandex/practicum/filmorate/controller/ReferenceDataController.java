package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.controller.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.dto.GenreDTO;
import ru.yandex.practicum.filmorate.model.dto.MpaDTO;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping
public class ReferenceDataController {
    @GetMapping("/genres")
    public List<GenreDTO> getAllGenres() {
        return Arrays.stream(Genre.values())
                .map(g -> new GenreDTO(g.ordinal() + 1, g.name()))
                .collect(Collectors.toList());
    }


    @GetMapping("/genres/{id}")
    public Map<String, Object> getGenreById(@PathVariable int id) {
        Genre[] values = Genre.values();
        if (id < 1 || id > values.length) {
            throw new NotFoundException("Жанр с id " + id + " не найден");
        }
        Genre g = values[id - 1];
        return Map.of("id", id, "name", g.name());
    }

    @GetMapping("/mpa")
    public List<MpaDTO> getAllMpa() {
        return Arrays.stream(MpaRating.values())
                .map(r -> new MpaDTO(r.ordinal() + 1, r.name()))
                .collect(Collectors.toList());
    }

    @GetMapping("/mpa/{id}")
    public MpaDTO getMpaById(@PathVariable int id) {
        if (id < 1 || id > MpaRating.values().length) {
            throw new NotFoundException("MPA с id " + id + " не найден");
        }
        MpaRating r = MpaRating.values()[id - 1];
        return new MpaDTO(id, r.name());
    }
}