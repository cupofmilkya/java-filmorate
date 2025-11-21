package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.mappers.dto.FilmDTOMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.dto.DirectorDTO;
import ru.yandex.practicum.filmorate.model.dto.FilmDTO;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;
    private final DirectorStorage directorStorage;

    @GetMapping
    public ResponseEntity<List<FilmDTO>> getFilms() {
        List<FilmDTO> films = filmService.getFilms().stream()
                .map(FilmDTOMapper::convertToDto)
                .collect(Collectors.toList());

        loadDirectorsNames(films);
        return ResponseEntity.ok(films);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilmDTO> getFilmById(@PathVariable Long id) {
        FilmDTO filmDto = FilmDTOMapper.convertToDto(filmService.getFilm(id));
        loadDirectorsNames(List.of(filmDto));
        return ResponseEntity.ok(filmDto);
    }

    @PostMapping
    public ResponseEntity<FilmDTO> createFilm(@Valid @RequestBody FilmDTO filmDTO) {
        FilmDTO filmDto = FilmDTOMapper.convertToDto(filmService.addFilm(FilmDTOMapper.convertToFilm(filmDTO)));
        loadDirectorsNames(List.of(filmDto));
        return ResponseEntity.status(HttpStatus.CREATED).body(filmDto);
    }

    @PutMapping
    public ResponseEntity<FilmDTO> updateFilm(@Valid @RequestBody FilmDTO filmDTO) {
        FilmDTO filmDto = FilmDTOMapper.convertToDto(filmService.updateFilm(FilmDTOMapper.convertToFilm(filmDTO)));
        loadDirectorsNames(List.of(filmDto));
        return ResponseEntity.ok(filmDto);
    }

    @DeleteMapping("/{id}")
    public void removeFilm(@PathVariable long id) {
        filmService.removeFilm(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<FilmDTO> addLike(@PathVariable long id, @PathVariable long userId) {
        FilmDTO filmDto = FilmDTOMapper.convertToDto(filmService.sendLike(id, userId));
        loadDirectorsNames(List.of(filmDto));
        return ResponseEntity.ok(filmDto);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<FilmDTO> removeLike(@PathVariable long id, @PathVariable long userId) {
        FilmDTO filmDto = FilmDTOMapper.convertToDto(filmService.removeLike(id, userId));
        loadDirectorsNames(List.of(filmDto));
        return ResponseEntity.ok(filmDto);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<FilmDTO>> getPopularFilms(@RequestParam(defaultValue = "10") long count,
                                                         @RequestParam(required = false) Long genreId,
                                                         @RequestParam(required = false) Long year) {
        List<FilmDTO> films;
        if ((genreId == null) && (year == null)) {
            films = filmService.getPopularFilms((int) count).stream()
                    .map(FilmDTOMapper::convertToDto)
                    .collect(Collectors.toList());
        } else {
            films = filmService.getPopularFilms(count, genreId, year).stream()
                    .map(FilmDTOMapper::convertToDto)
                    .collect(Collectors.toList());
        }
        loadDirectorsNames(films);
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

        loadDirectorsNames(films);
        return ResponseEntity.ok(films);
    }

    @GetMapping("/common")
    public ResponseEntity<List<FilmDTO>> getCommonFilms(
            @RequestParam long userId,
            @RequestParam long friendId) {

        List<FilmDTO> films = filmService.getCommonFilms(userId, friendId).stream()
                .map(FilmDTOMapper::convertToDto)
                .collect(Collectors.toList());

        loadDirectorsNames(films);
        return ResponseEntity.ok(films);
    }

    @GetMapping("/search")
    public ResponseEntity<List<FilmDTO>> searchFilms(
            @RequestParam String query,
            @RequestParam String by) {

        List<FilmDTO> films = filmService.searchFilms(query, by).stream()
                .map(FilmDTOMapper::convertToDto)
                .collect(Collectors.toList());

        loadDirectorsNames(films);
        return ResponseEntity.ok(films);
    }

    private void loadDirectorsNames(List<FilmDTO> filmDTOs) {
        for (FilmDTO dto : filmDTOs) {
            if (dto.getDirectors() != null && !dto.getDirectors().isEmpty()) {
                Set<Long> directorIds = dto.getDirectors().stream()
                        .map(DirectorDTO::getId)
                        .collect(Collectors.toSet());

                Set<Director> directors = directorStorage.getDirectorsByIds(directorIds);
                Set<DirectorDTO> directorsDto = directors.stream()
                        .map(d -> new DirectorDTO(d.getId(), d.getName()))
                        .collect(Collectors.toCollection(LinkedHashSet::new));

                dto.setDirectors(directorsDto);
            }
        }
    }
}