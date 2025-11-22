package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.mappers.dto.DirectorDTOMapper;
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
        return ResponseEntity.ok(directorService.getDirectorsDto());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DirectorDTO> getDirector(@PathVariable Long id) {
        return ResponseEntity.ok(directorService.getDirectorDto(id));
    }

    @PostMapping()
    public ResponseEntity<DirectorDTO> addDirector(@Valid @RequestBody DirectorDTO dto) {
        DirectorDTO directorDTO =
                DirectorDTOMapper.convertToDto(directorService.addDirector(DirectorDTOMapper.convertToDirector(dto)));

        return ResponseEntity.ok(directorDTO);
    }

    @PutMapping()
    public ResponseEntity<DirectorDTO> updateDirector(@Valid @RequestBody DirectorDTO director) {
        return ResponseEntity.ok(directorService.updateDirectorDto(director));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DirectorDTO> deleteDirector(@PathVariable Long id) {
        return ResponseEntity.ok(directorService.deleteDirectorDto(id));
    }
}