package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.dto.DirectorDTO;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@RequestMapping("/directors")
public class DirectorController {
    @Autowired
    private DirectorService directorService;

    @GetMapping
    public ResponseEntity<List<DirectorDTO>> getDirectors() {
        List<DirectorDTO> directors = directorService.getDirectors().stream()
                .map(this::convertToDto)
                .toList();

        return ResponseEntity.ok(directors);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DirectorDTO> getDirector(@PathVariable Long id) {
        DirectorDTO directorDTO = convertToDto(directorService.getDirector(id));

        return ResponseEntity.ok(directorDTO);
    }

    @PostMapping()
    public ResponseEntity<DirectorDTO> addDirector(@RequestBody DirectorDTO director) {
        DirectorDTO directorDTO = convertToDto(directorService.addDirector(convertToDirector(director)));

        return ResponseEntity.ok(directorDTO);
    }

    @PutMapping()
    public ResponseEntity<DirectorDTO> updateDirector(@RequestBody DirectorDTO director) {
        Director directorUpdated = directorService.updateDirector(convertToDirector(director));

        return ResponseEntity.ok(convertToDto(directorUpdated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DirectorDTO> deleteDirector(@PathVariable Long id) {
        Director director = directorService.deleteDirector(id);

        return ResponseEntity.ok(convertToDto(director));
    }

    private DirectorDTO convertToDto(Director director) {
        return DirectorDTO.builder()
                .id(director.getId())
                .name(director.getName())
                .build();
    }

    private Director convertToDirector(DirectorDTO dto) {
        return Director.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }
}