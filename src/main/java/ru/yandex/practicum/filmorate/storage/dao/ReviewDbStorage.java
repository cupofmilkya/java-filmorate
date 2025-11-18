package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.controller.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    private static final String SELECT_REVIEW_WITH_USEFUL = """
            SELECT
                r.review_id,
                r.content,
                r.is_positive,
                r.user_id,
                r.film_id,
                COALESCE(SUM(CASE
                    WHEN v.is_like IS TRUE  THEN  1
                    WHEN v.is_like IS FALSE THEN -1
                    ELSE 0
                END), 0) AS useful
            FROM reviews r
            LEFT JOIN review_votes v ON v.review_id = r.review_id
            """;

    private static final String SQL_GET_ONE = SELECT_REVIEW_WITH_USEFUL + """
        WHERE r.review_id = ?
        GROUP BY r.review_id, r.content, r.is_positive, r.user_id, r.film_id
        """;

    private static final String SQL_GET_ALL = SELECT_REVIEW_WITH_USEFUL + """
        GROUP BY r.review_id, r.content, r.is_positive, r.user_id, r.film_id
        ORDER BY useful DESC, r.review_id ASC
        LIMIT ?
        """;

    private static final String SQL_GET_BY_FILM = SELECT_REVIEW_WITH_USEFUL + """
        WHERE r.film_id = ?
        GROUP BY r.review_id, r.content, r.is_positive, r.user_id, r.film_id
        ORDER BY useful DESC, r.review_id ASC
        LIMIT ?
        """;

    @Override
    public void addReview(Review review) {
        String sql = "INSERT INTO reviews(content, is_positive, user_id, film_id) VALUES (?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            return ps;
        }, keyHolder);
        review.setReviewId(Objects.requireNonNull(keyHolder.getKey()).longValue());
    }

    @Override
    public Optional<Review> getReview(Long reviewId) {
        try {
            Review review = jdbcTemplate.queryForObject(SQL_GET_ONE, new ReviewRowMapper(), reviewId);
            return Optional.ofNullable(review);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Review> getReviewsByFilm(Long filmId, int count) {
        return (filmId == null)
                ? jdbcTemplate.query(SQL_GET_ALL, new ReviewRowMapper(), count)
                : jdbcTemplate.query(SQL_GET_BY_FILM, new ReviewRowMapper(), filmId, count);
    }

    @Override
    public void updateReview(Review review) {
        final String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
        int updated = jdbcTemplate.update(sql,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());

        if (updated == 0) {
            throw new NotFoundException("Review id=" + review.getReviewId() + " не найден!");
        }
    }

    @Override
    public void deleteReview(Long reviewId) {
        jdbcTemplate.update("DELETE FROM reviews WHERE review_id = ?", reviewId);
    }

    @Override
    public void addEstimate(Long reviewId, Long userId, boolean isLike) {
        jdbcTemplate.update(
                "MERGE INTO review_votes (review_id, user_id, is_like) KEY (review_id, user_id) VALUES (?,?,?)",
                reviewId, userId, isLike
        );
    }

    @Override
    public void deleteEstimate(Long reviewId, long userId) {
        jdbcTemplate.update(
                "DELETE FROM review_votes WHERE review_id = ? AND user_id = ?",
                reviewId, userId
        );
    }

    @Override
    public void deleteDislike(long reviewId, long userId) {
        jdbcTemplate.update(
                "DELETE FROM review_votes WHERE review_id = ? AND user_id = ? AND is_like = FALSE",
                reviewId, userId
        );
    }
}
