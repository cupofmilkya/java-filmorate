package ru.yandex.practicum.filmorate.model.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class DirectorDTO {

    private Long id;

    @NotBlank(message = "Имя режиссёра не может быть пустым")
    private String name;
}