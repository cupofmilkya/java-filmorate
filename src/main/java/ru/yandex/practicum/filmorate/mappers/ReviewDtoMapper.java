package ru.yandex.practicum.filmorate.mappers;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.dto.ReviewDTO;

@UtilityClass
public class ReviewDtoMapper {

    public Review toDomain(ReviewDTO d) {
        if (d == null) return null;
        return Review.builder()
                .reviewId(d.getReviewId())
                .content(d.getContent())
                .isPositive(d.getIsPositive())
                .userId(d.getUserId())
                .filmId(d.getFilmId())
                .useful(d.getUseful())
                .build();
    }

    public ReviewDTO toDto(Review r) {
        if (r == null) return null;
        return ReviewDTO.builder()
                .reviewId(r.getReviewId())
                .content(r.getContent())
                .isPositive(r.getIsPositive())
                .userId(r.getUserId())
                .filmId(r.getFilmId())
                .useful(r.getUseful())
                .build();
    }
}
