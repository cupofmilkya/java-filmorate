package ru.yandex.practicum.filmorate.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import jakarta.validation.constraints.*;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ReviewDTO {

    @Null(groups = OnCreate.class,  message = "reviewId не передаём при создании")
    @NotNull(groups = OnUpdate.class, message = "reviewId обязателен при обновлении")
    @Positive(groups = OnUpdate.class, message = "reviewId должен быть > 0")
    private Long reviewId;

    @NotBlank(message = "content не должен быть пустым")
    @Size(max = 1000, message = "content не длиннее 1000 символов")
    private String content;

    @NotNull(message = "isPositive обязателен")
    private Boolean isPositive;

    @NotNull(message = "userId обязателен")
    @Positive(message = "userId должен быть > 0")
    private Long userId;

    @NotNull(message = "filmId обязателен")
    @Positive(message = "filmId должен быть > 0")
    private Long filmId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer useful;

    public interface OnCreate {}
    public interface OnUpdate {}
}
