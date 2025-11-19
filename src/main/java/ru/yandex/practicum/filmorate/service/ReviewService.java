package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.controller.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final FeedStorage feedStorage;

    public Review create(Review review) {
        checkUser(review.getUserId());
        checkFilm(review.getFilmId());
        review.setUseful(0);
        Review saved = reviewStorage.addReview(review);
        feedStorage.saveEvent(review.getUserId(), EventType.REVIEW, Operation.ADD, review.getReviewId());

        return saved;
    }

    public Review getById(long reviewId) {
        checkReview(reviewId);
        return reviewStorage.getReview(reviewId).get();
    }

    public List<Review> getAll(int count) {
        return reviewStorage.getReviewsByUseful(count);
    }

    public List<Review> getReviewsByFilm(Long filmId, int count) {
        checkFilm(filmId);
        return reviewStorage.getReviewsByFilm(filmId, count);
    }

    public Review update(Review review) {
        if (getById(review.getReviewId()) == null) {
            throw new NotFoundException("Review c id %d не найден!".formatted(review.getReviewId()));
        }

        Review existing = getById(review.getReviewId());
        review.setUserId(existing.getUserId());
        review.setFilmId(existing.getFilmId());

        Review updated = reviewStorage.updateReview(review);
        feedStorage.saveEvent(updated.getUserId(), EventType.REVIEW, Operation.UPDATE, updated.getReviewId());

        return updated;
    }

    public void deleteById(long reviewId) {
        checkReview(reviewId);
        Review review = getById(reviewId);
        feedStorage.saveEvent(review.getUserId(), EventType.REVIEW, Operation.REMOVE, review.getReviewId());
        reviewStorage.deleteReview(reviewId);
    }

    public Review putLike(long reviewId, long userId) {
        checkReview(reviewId);
        checkUser(userId);

        reviewStorage.addEstimate(reviewId, userId, true);

        Optional<Review> review = reviewStorage.getReview(reviewId);
        if (!review.isPresent()) {
            throw new NotFoundException("Review c id %d не найден!".formatted(reviewId));
        }
        return review.get();
    }

    public Review putDislike(long reviewId, long userId) {
        checkReview(reviewId);
        checkUser(userId);

        reviewStorage.addEstimate(reviewId, userId, false);

        Optional<Review> review = reviewStorage.getReview(reviewId);
        if (!review.isPresent()) {
            throw new NotFoundException("Review c id %d не найден!".formatted(reviewId));
        }
        return review.get();
    }

    public void deleteEstimate(long reviewId, long userId) {
        checkReview(reviewId);
        checkUser(userId);
        reviewStorage.deleteEstimate(reviewId, userId);
    }

    public void deleteDislike(long reviewId, long userId) {
        checkReview(reviewId);
        checkUser(userId);
        reviewStorage.deleteDislike(reviewId, userId);
    }

    private void checkReview(long reviewId) {
        if (reviewStorage.getReview(reviewId).isEmpty()) {
            throw new NotFoundException("Review c id %d не найден!".formatted(reviewId));
        }
    }

    private void checkUser(long userId) {
        if (userStorage.getUser(userId) == null) {
            throw new NotFoundException("User c id %d не найден!".formatted(userId));
        }
    }

    private void checkFilm(long filmId) {
        if (filmStorage.getFilm(filmId) == null) {
            throw new NotFoundException("Film c id %d не найден!".formatted(filmId));
        }
    }
}
