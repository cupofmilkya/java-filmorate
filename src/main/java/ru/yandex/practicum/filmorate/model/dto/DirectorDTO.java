package ru.yandex.practicum.filmorate.model.dto;

import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class DirectorDTO {
    private Long id;
    private String name;
}
