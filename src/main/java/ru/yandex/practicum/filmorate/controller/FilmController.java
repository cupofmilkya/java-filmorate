package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.controller.exception.NotFoundException;
import ru.yandex.practicum.filmorate.controller.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.dto.FilmDTO;
import ru.yandex.practicum.filmorate.model.dto.GenreDTO;
import ru.yandex.practicum.filmorate.model.dto.MpaDTO;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/films")
public class FilmController {

    @Autowired
    private FilmService filmService;

    @GetMapping
    public ResponseEntity<List<FilmDTO>> getFilms() {
        List<FilmDTO> films = filmService.getFilms().stream()
                .map(this::convertToDto)
                .toList();

        return ResponseEntity.ok(films);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilmDTO> getFilmById(@PathVariable Long id) {
        FilmDTO filmdto = convertToDto(filmService.getFilm(id));

        return ResponseEntity.ok(filmdto);
    }

    @PostMapping
    public ResponseEntity<FilmDTO> createFilm(@RequestBody FilmDTO filmDTO) {
        FilmDTO filmdto = convertToDto(filmService.addFilm(convertToFilm(filmDTO)));

        return ResponseEntity.status(HttpStatus.CREATED).body(filmdto);
    }

    @PutMapping
    public ResponseEntity<FilmDTO> updateFilm(@RequestBody FilmDTO filmDTO) {
        FilmDTO filmdto = convertToDto(filmService.updateFilm(convertToFilm(filmDTO)));

        return ResponseEntity.ok(filmdto);
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<FilmDTO> addLike(@PathVariable long id, @PathVariable long userId) {
        FilmDTO filmdto = convertToDto(filmService.sendLike(id, userId));

        return ResponseEntity.ok(filmdto);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<FilmDTO> removeLike(@PathVariable long id, @PathVariable long userId) {
        FilmDTO filmdto = convertToDto(filmService.removeLike(id, userId));

        return ResponseEntity.ok(filmdto);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<FilmDTO>> getPopularFilms(@RequestParam(defaultValue = "10") long count,
                                                         @RequestParam(required = false) Long genreId,
                                                         @RequestParam(required = false) Long year) {
        List<FilmDTO> films;
        if ((genreId == null) && (year == null)) {
            films = filmService.getPopularFilms((int) count).stream()
                    .map(this::convertToDto)
                    .toList();
        } else {
            films = filmService.getPopularFilms(count, genreId, year).stream()
                    .map(this::convertToDto)
                    .toList();
        }
        return ResponseEntity.ok(films);
    }

    private FilmDTO convertToDto(Film film) {
        return FilmDTO.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpa(film.getMpaRating() != null ? MpaDTO.fromEnum(film.getMpaRating()) : null)
                .genres(film.getGenres() != null && !film.getGenres().isEmpty()
                        ? film.getGenres().stream()
                        .map(GenreDTO::fromEnum)
                        .sorted(Comparator.comparingInt(GenreDTO::getId))
                        .collect(Collectors.toCollection(LinkedHashSet::new))
                        : new LinkedHashSet<>())
                .build();
    }

    private Film convertToFilm(FilmDTO dto) {
        Film film = new Film();
        film.setId(dto.getId());
        film.setName(dto.getName());
        film.setDescription(dto.getDescription());
        film.setReleaseDate(dto.getReleaseDate());
        film.setDuration(dto.getDuration());

        if (dto.getMpa() != null) {
            int mpaId = dto.getMpa().getId();
            if (mpaId > 0 && mpaId <= MpaRating.values().length) {
                film.setMpaRating(MpaRating.values()[mpaId - 1]);
            } else {
                throw new NotFoundException("Не существует MPA с ID: " + mpaId);
            }
        } else {
            throw new ValidationException("Не указан ID MPA");
        }

        if (dto.getGenres() != null && !dto.getGenres().isEmpty()) {
            Set<Genre> genres = dto.getGenres().stream()
                    .map(g -> {
                        int index = g.getId() - 1;
                        if (index < 0 || index >= Genre.values().length) {
                            throw new NotFoundException("Не существует жанра с ID: " + g.getId());
                        }
                        return Genre.values()[index];
                    })
                    .collect(Collectors.toSet());
            film.setGenres(genres);
        } else {
            film.setGenres(Set.of());
        }

        return film;
    }
}