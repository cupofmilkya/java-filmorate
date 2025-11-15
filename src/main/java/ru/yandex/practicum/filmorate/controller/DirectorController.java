package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.mappers.dto.DirectorDTOMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.dto.DirectorDTO;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public ResponseEntity<List<DirectorDTO>> getDirectors() {
        List<DirectorDTO> directors = directorService.getDirectors().stream()
                .map(DirectorDTOMapper::convertToDto)
                .toList();

        return ResponseEntity.ok(directors);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DirectorDTO> getDirector(@PathVariable Long id) {
        DirectorDTO directorDTO = DirectorDTOMapper.convertToDto(directorService.getDirector(id));

        return ResponseEntity.ok(directorDTO);
    }

    @PostMapping()
    public ResponseEntity<DirectorDTO> addDirector(@Valid @RequestBody DirectorDTO dto) {
        DirectorDTO directorDTO =
                DirectorDTOMapper.convertToDto(directorService.addDirector(DirectorDTOMapper.convertToDirector(dto)));

        return ResponseEntity.ok(directorDTO);
    }

    @PutMapping()
    public ResponseEntity<DirectorDTO> updateDirector(@Valid @RequestBody DirectorDTO director) {
        Director directorUpdated = directorService.updateDirector(DirectorDTOMapper.convertToDirector(director));

        return ResponseEntity.ok(DirectorDTOMapper.convertToDto(directorUpdated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DirectorDTO> deleteDirector(@PathVariable Long id) {
        Director director = directorService.deleteDirector(id);

        return ResponseEntity.ok(DirectorDTOMapper.convertToDto(director));
    }
}