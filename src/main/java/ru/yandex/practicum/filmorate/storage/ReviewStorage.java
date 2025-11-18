package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {

    void addReview(Review review);

    Optional<Review> getReview(Long reviewId);

    List<Review> getReviewsByFilm(Long filmId, int count);

    void updateReview(Review review);

    void deleteReview(Long reviewId);

    void addEstimate(Long reviewId, Long userId, boolean isLike);

    void deleteEstimate(Long reviewId, long userId);

    void deleteDislike(long reviewId, long userId);
}

