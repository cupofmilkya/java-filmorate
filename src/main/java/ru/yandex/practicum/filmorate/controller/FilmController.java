package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.dto.FilmDTO;
import ru.yandex.practicum.filmorate.model.dto.MpaDTO;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;
import java.util.Set;
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

    @PutMapping("/{id}")
    public FilmDTO updateFilm(@PathVariable Long id, @RequestBody FilmDTO filmDTO) {
        Film film = convertToFilm(filmDTO);
        film.setId(id);
        return convertToDto(filmService.updateFilm(film));
    }

    private FilmDTO convertToDto(Film film) {
        return FilmDTO.builder()
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpa(film.getMpaRating() != null
                        ? new MpaDTO(film.getMpaRating().ordinal() + 1, film.getMpaRating().toString())
                        : null)
                .genreIds(film.getGenres() != null
                        ? film.getGenres().stream().map(Enum::ordinal).map(i -> i + 1).collect(Collectors.toSet())
                        : null)
                .build();
    }

    private Film convertToFilm(FilmDTO dto) {
        Film film = new Film();
        film.setName(dto.getName());
        film.setDescription(dto.getDescription());
        film.setReleaseDate(dto.getReleaseDate());
        film.setDuration(dto.getDuration());

        if (dto.getMpa() != null) {
            int mpaId = dto.getMpa().getId();
            film.setMpaRating(MpaRating.values()[mpaId - 1]);
        }

        if (dto.getGenreIds() != null) {
            Set<Genre> genres = dto.getGenreIds().stream()
                    .map(id -> Genre.values()[id - 1])
                    .collect(Collectors.toSet());
            film.setGenres(genres);
        }

        return film;
    }
}