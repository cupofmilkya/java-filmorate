package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.controller.exception.ValidationException;
import ru.yandex.practicum.filmorate.mappers.ReviewDtoMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.dto.ReviewDTO;
import ru.yandex.practicum.filmorate.storage.ReviewService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ReviewDTO create(@RequestBody ReviewDTO dto) {
        log.info("POST /reviews : создать отзыв для user={}, film={}", dto.getUserId(), dto.getFilmId());
        Review createdReview = reviewService.create(ReviewDtoMapper.toDomain(dto));
        return ReviewDtoMapper.toDto(createdReview);
    }

    @PutMapping
    public ReviewDTO update(@RequestBody ReviewDTO dto) {
        log.info("PUT /reviews : обновление review id={}", dto.getReviewId());
        Review updatedReview = reviewService.update(ReviewDtoMapper.toDomain(dto));
        return ReviewDtoMapper.toDto(updatedReview);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        log.info("DELETE /reviews/{}", id);
        reviewService.deleteById(id);
    }

    @GetMapping("/{id}")
    public ReviewDTO getById(@PathVariable long id) {
        log.info("GET /reviews/{}", id);
        return ReviewDtoMapper.toDto(reviewService.getById(id));
    }

    @GetMapping
    public List<ReviewDTO> getAll(@RequestParam(required = false) Long filmId,
                                  @RequestParam(defaultValue = "10") int count) {
        if (count <= 0) throw new ValidationException("count должен быть положительным числом!");
        log.info("GET /reviews?filmId={}&count={}", filmId, count);
        return reviewService.getReviewsByFilm(filmId, count)
                .stream()
                .map(ReviewDtoMapper::toDto)
                .toList();
    }

    @PutMapping("/{id}/like/{userId}")
    public ReviewDTO putLike(@PathVariable long id, @PathVariable long userId) {
        log.info("PUT /reviews/{}/like/{}", id, userId);
        return ReviewDtoMapper.toDto(reviewService.putLike(id, userId));
    }

    @PutMapping("/{id}/dislike/{userId}")
    public ReviewDTO putDislike(@PathVariable long id, @PathVariable long userId) {
        log.info("PUT /reviews/{}/dislike/{}", id, userId);
        return ReviewDtoMapper.toDto(reviewService.putDislike(id, userId));
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ReviewDTO deleteLike(@PathVariable long id, @PathVariable long userId) {
        log.info("DELETE /reviews/{}/like/{}", id, userId);
        reviewService.deleteEstimate(id, userId);
        return ReviewDtoMapper.toDto(reviewService.getById(id));
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public ReviewDTO deleteDislike(@PathVariable long id, @PathVariable long userId) {
        log.info("DELETE /reviews/{}/dislike/{}", id, userId);
        reviewService.deleteDislike(id, userId);       // под капотом: DELETE ... AND is_like=false
        return ReviewDtoMapper.toDto(reviewService.getById(id));
    }
}
