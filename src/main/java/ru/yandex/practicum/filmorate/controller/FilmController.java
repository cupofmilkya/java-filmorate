package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.dto.FilmDTO;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public ResponseEntity<List<FilmDTO>> getFilms() {
        return ResponseEntity.ok(filmService.getFilmsDto());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilmDTO> getFilmById(@PathVariable Long id) {
        return ResponseEntity.ok(filmService.getFilmDtoById(id));
    }

    @PostMapping
    public ResponseEntity<FilmDTO> createFilm(@Valid @RequestBody FilmDTO filmDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(filmService.addFilmDto(filmDTO));
    }

    @PutMapping
    public ResponseEntity<FilmDTO> updateFilm(@Valid @RequestBody FilmDTO filmDTO) {
        return ResponseEntity.ok(filmService.updateFilmDto(filmDTO));
    }

    @DeleteMapping("/{id}")
    public void removeFilm(@PathVariable long id) {
        filmService.removeFilm(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<FilmDTO> addLike(@PathVariable long id, @PathVariable long userId) {
        return ResponseEntity.ok(filmService.sendLikeDto(id, userId));
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<FilmDTO> removeLike(@PathVariable long id, @PathVariable long userId) {
        return ResponseEntity.ok(filmService.removeLikeDto(id, userId));
    }

    @GetMapping("/popular")
    public ResponseEntity<List<FilmDTO>> getPopularFilms(@RequestParam(defaultValue = "10") long count,
                                                         @RequestParam(required = false) Long genreId,
                                                         @RequestParam(required = false) Long year) {
        return ResponseEntity.ok(filmService.getPopularFilmsDto(count, genreId, year));
    }

    @GetMapping("/director/{directorId}")
    public ResponseEntity<List<FilmDTO>> getFilmsByDirector(@PathVariable Long directorId,
                                                            @RequestParam(defaultValue = "year") String sortBy) {
        return ResponseEntity.ok(filmService.getFilmsByDirectorDto(directorId, sortBy));
    }

    @GetMapping("/common")
    public ResponseEntity<List<FilmDTO>> getCommonFilms(@RequestParam long userId,
                                                        @RequestParam long friendId) {
        return ResponseEntity.ok(filmService.getCommonFilmsDto(userId, friendId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<FilmDTO>> searchFilms(@RequestParam String query,
                                                     @RequestParam String by) {
        return ResponseEntity.ok(filmService.searchFilmsDto(query, by));
    }
}