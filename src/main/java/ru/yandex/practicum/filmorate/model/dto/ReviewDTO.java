package ru.yandex.practicum.filmorate.model.dto;

import lombok.*;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ReviewDTO {

    private Long reviewId;

    @jakarta.validation.constraints.NotBlank
    private String content;

    @jakarta.validation.constraints.NotNull
    private Boolean isPositive;

    @jakarta.validation.constraints.NotNull
    private Long userId;

    @jakarta.validation.constraints.NotNull
    private Long filmId;

    private Integer useful;
}

