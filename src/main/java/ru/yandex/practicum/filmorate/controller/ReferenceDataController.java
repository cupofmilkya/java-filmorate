package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<GenreDTO>> getAllGenres() {
        List<GenreDTO> genres = Arrays.stream(Genre.values())
                .map(GenreDTO::fromEnum)
                .collect(Collectors.toList());
        return ResponseEntity.ok(genres);
    }

    @GetMapping("/genres/{id}")
    public ResponseEntity<GenreDTO> getGenreById(@PathVariable int id) {
        if (id < 1 || id > Genre.values().length) {
            throw new NotFoundException("Жанр с id " + id + " не найден");
        }
        GenreDTO genre = GenreDTO.fromEnum(Genre.values()[id - 1]);
        return ResponseEntity.ok(genre);
    }

    @GetMapping("/mpa")
    public ResponseEntity<List<MpaDTO>> getAllMpa() {
        List<MpaDTO> mpaList = Arrays.stream(MpaRating.values())
                .map(MpaDTO::fromEnum)
                .collect(Collectors.toList());
        return ResponseEntity.ok(mpaList);
    }

    @GetMapping("/mpa/{id}")
    public ResponseEntity<MpaDTO> getMpaById(@PathVariable int id) {
        if (id < 1 || id > MpaRating.values().length) {
            throw new NotFoundException("MPA с id " + id + " не найден");
        }
        MpaDTO mpa = MpaDTO.fromEnum(MpaRating.values()[id - 1]);
        return ResponseEntity.ok(mpa);
    }
}