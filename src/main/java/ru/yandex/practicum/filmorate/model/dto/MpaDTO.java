package ru.yandex.practicum.filmorate.model.dto;

import lombok.*;
import ru.yandex.practicum.filmorate.model.MpaRating;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class MpaDTO {
    private int id;
    private String name;

    public static MpaDTO fromEnum(MpaRating rating) {
        return new MpaDTO(rating.ordinal() + 1, switch (rating) {
            case G -> "G";
            case PG -> "PG";
            case PG13 -> "PG-13";
            case R -> "R";
            case NC17 -> "NC-17";
        });
    }
}