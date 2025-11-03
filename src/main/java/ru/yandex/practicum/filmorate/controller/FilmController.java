package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
    public List<FilmDTO> getFilms() {
        return filmService.getFilms().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public FilmDTO getFilmById(@PathVariable Long id) {
        return convertToDto(filmService.getFilm(id));
    }

    @PostMapping
    public FilmDTO createFilm(@RequestBody FilmDTO filmDTO) {
        Film film = filmService.addFilm(convertToFilm(filmDTO));
        return convertToDto(film);
    }

    @PutMapping
    public FilmDTO updateFilm(@RequestBody FilmDTO filmDTO) {
        Film film = convertToFilm(filmDTO);
        return convertToDto(filmService.updateFilm(film));
    }

    @PutMapping("/{id}/like/{userId}")
    public FilmDTO addLike(@PathVariable long id, @PathVariable long userId) {
        Film film = filmService.sendLike(id, userId);
        return convertToDto(film);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public FilmDTO removeLike(@PathVariable long id, @PathVariable long userId) {
        Film film = filmService.removeLike(id, userId);
        return convertToDto(film);
    }

    private FilmDTO convertToDto(Film film) {
        return FilmDTO.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpa(film.getMpaRating() != null ? MpaDTO.fromEnum(film.getMpaRating()) : null)
                .genres(film.getGenres().stream()
                        .map(GenreDTO::fromEnum)
                        .sorted(Comparator.comparingInt(GenreDTO::getId)) // сортировка по id
                        .collect(Collectors.toCollection(LinkedHashSet::new))) // сохраняем порядок
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

    @GetMapping("/popular")
    public List<FilmDTO> getPopularFilms(@RequestParam(defaultValue = "10") long count) {
        return filmService.getPopularFilms((int) count).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}