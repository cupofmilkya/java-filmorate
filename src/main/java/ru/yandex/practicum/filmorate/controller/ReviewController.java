package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.mappers.ReviewDtoMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.dto.ReviewDTO;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewDTO> create(@Valid @RequestBody ReviewDTO dto) {
        log.info("POST /reviews : создать отзыв для user={}, film={}", dto.getUserId(), dto.getFilmId());
        Review createdReview = reviewService.create(ReviewDtoMapper.toDomain(dto));
        return ResponseEntity.ok(ReviewDtoMapper.toDto(createdReview));
    }

    @PutMapping
    public ResponseEntity<ReviewDTO> update(@Valid @RequestBody ReviewDTO dto) {
        log.info("PUT /reviews : обновление review id={}", dto.getReviewId());
        Review updatedReview = reviewService.update(ReviewDtoMapper.toDomain(dto));
        return ResponseEntity.ok(ReviewDtoMapper.toDto(updatedReview));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        log.info("DELETE /reviews/{}", id);
        reviewService.deleteById(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDTO> getById(@PathVariable long id) {
        log.info("GET /reviews/{}", id);
        return ResponseEntity.ok(ReviewDtoMapper.toDto(reviewService.getById(id)));
    }

    @GetMapping
    public List<Review> getAll(
            @RequestParam(required = false) Long filmId,
            @RequestParam(defaultValue = "10") Integer count
    ) {
        log.info("GET /reviews?filmId={}&count={}", filmId, count);
        int limit = (count == null || count <= 0) ? 10 : count;
        return (filmId == null)
                ? reviewService.getAll(limit)
                : reviewService.getReviewsByFilm(filmId, limit);
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<ReviewDTO> putLike(@PathVariable long id, @PathVariable long userId) {
        log.info("PUT /reviews/{}/like/{}", id, userId);
        return ResponseEntity.ok(ReviewDtoMapper.toDto(reviewService.putLike(id, userId)));
    }

    @PutMapping("/{id}/dislike/{userId}")
    public ResponseEntity<ReviewDTO> putDislike(@PathVariable long id, @PathVariable long userId) {
        log.info("PUT /reviews/{}/dislike/{}", id, userId);
        return ResponseEntity.ok(ReviewDtoMapper.toDto(reviewService.putDislike(id, userId)));
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<ReviewDTO> deleteLike(@PathVariable long id, @PathVariable long userId) {
        log.info("DELETE /reviews/{}/like/{}", id, userId);
        reviewService.deleteEstimate(id, userId);
        return ResponseEntity.ok(ReviewDtoMapper.toDto(reviewService.getById(id)));
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public ResponseEntity<ReviewDTO> deleteDislike(@PathVariable long id, @PathVariable long userId) {
        log.info("DELETE /reviews/{}/dislike/{}", id, userId);
        reviewService.deleteDislike(id, userId);       // под капотом: DELETE ... AND is_like=false
        return ResponseEntity.ok(ReviewDtoMapper.toDto(reviewService.getById(id)));
    }
}
