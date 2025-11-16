package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.mappers.dto.FilmDTOMapper;
import ru.yandex.practicum.filmorate.model.dto.FilmDTO;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public ResponseEntity<List<FilmDTO>> getFilms() {
        List<FilmDTO> films = filmService.getFilms().stream()
                .map(FilmDTOMapper::convertToDto)
                .toList();

        return ResponseEntity.ok(films);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilmDTO> getFilmById(@PathVariable Long id) {
        FilmDTO filmdto = FilmDTOMapper.convertToDto(filmService.getFilm(id));

        return ResponseEntity.ok(filmdto);
    }

    @PostMapping
    public ResponseEntity<FilmDTO> createFilm(@Valid @RequestBody FilmDTO filmDTO) {
        FilmDTO filmdto = FilmDTOMapper.convertToDto(filmService.addFilm(FilmDTOMapper.convertToFilm(filmDTO)));

        return ResponseEntity.status(HttpStatus.CREATED).body(filmdto);
    }

    @PutMapping
    public ResponseEntity<FilmDTO> updateFilm(@Valid @RequestBody FilmDTO filmDTO) {
        FilmDTO filmdto = FilmDTOMapper.convertToDto(filmService.updateFilm(FilmDTOMapper.convertToFilm(filmDTO)));

        return ResponseEntity.ok(filmdto);
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<FilmDTO> addLike(@PathVariable long id, @PathVariable long userId) {
        FilmDTO filmdto = FilmDTOMapper.convertToDto(filmService.sendLike(id, userId));

        return ResponseEntity.ok(filmdto);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<FilmDTO> removeLike(@PathVariable long id, @PathVariable long userId) {
        FilmDTO filmdto = FilmDTOMapper.convertToDto(filmService.removeLike(id, userId));

        return ResponseEntity.ok(filmdto);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<FilmDTO>> getPopularFilms(@RequestParam(defaultValue = "10") long count,
                                                         @RequestParam(required = false) Long genreId,
                                                         @RequestParam(required = false) Long year) {
        List<FilmDTO> films;
        if ((genreId == null) && (year == null)) {
            films = filmService.getPopularFilms((int) count).stream()
                    .map(FilmDTOMapper::convertToDto)
                    .toList();
        } else {
            films = filmService.getPopularFilms(count, genreId, year).stream()
                    .map(FilmDTOMapper::convertToDto)
                    .toList();
        }
        return ResponseEntity.ok(films);
    }

    @GetMapping("/director/{directorId}")
    public ResponseEntity<List<FilmDTO>> getFilmsByDirector(
            @PathVariable Long directorId,
            @RequestParam(name = "sortBy", defaultValue = "year") String sortBy
    ) {
        List<FilmDTO> films = filmService.getFilmsByDirector(directorId, sortBy).stream()
                .map(FilmDTOMapper::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(films);
    }
}