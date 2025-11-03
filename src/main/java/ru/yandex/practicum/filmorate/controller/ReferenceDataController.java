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
                .map(g -> new GenreDTO(g.ordinal() + 1, toDisplayName(g)))
                .collect(Collectors.toList());
    }

    @GetMapping("/genres/{id}")
    public GenreDTO getGenreById(@PathVariable int id) {
        if (id < 1 || id > Genre.values().length) {
            throw new NotFoundException("Жанр с id " + id + " не найден");
        }
        Genre g = Genre.values()[id - 1];
        return new GenreDTO(id, toDisplayName(g));
    }

    @GetMapping("/mpa")
    public List<MpaDTO> getAllMpa() {
        return Arrays.stream(MpaRating.values())
                .map(r -> new MpaDTO(r.ordinal() + 1, toDisplayName(r)))
                .collect(Collectors.toList());
    }

    @GetMapping("/mpa/{id}")
    public MpaDTO getMpaById(@PathVariable int id) {
        if (id < 1 || id > MpaRating.values().length) {
            throw new NotFoundException("MPA с id " + id + " не найден");
        }
        MpaRating r = MpaRating.values()[id - 1];
        return new MpaDTO(id, toDisplayName(r));
    }

    private String toDisplayName(Genre g) {
        return switch (g) {
            case COMEDY -> "Комедия";
            case DRAMA -> "Драма";
            case CARTOON -> "Мультфильм";
            case THRILLER -> "Триллер";
            case DOCUMENTARY -> "Документальный";
            case ACTION -> "Боевик";
        };
    }

    private String toDisplayName(MpaRating r) {
        return switch (r) {
            case G -> "G";
            case PG -> "PG";
            case PG13 -> "PG-13";
            case R -> "R";
            case NC17 -> "NC-17";
        };
    }
}