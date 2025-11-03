package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.controller.exception.FriendsAddingException;
import ru.yandex.practicum.filmorate.controller.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.mappers.UserMapper;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@Primary
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addUser(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().longValue());
    }

    @Override
    public User getUser(long id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        List<User> users = jdbcTemplate.query(sql, new UserMapper(), id);
        if (users.isEmpty()) return null;
        User user = users.getFirst();
        loadAllFriends(user);
        return user;
    }

    @Override
    public Map<Long, User> getUsers() {
        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, new UserMapper());
        for (User u : users) {
            loadAllFriends(u);
        }
        return users.stream().collect(Collectors.toMap(User::getId, u -> u));
    }

    @Override
    public void updateUser(long id, User user) {
        String sql = "UPDATE users SET email=?, login=?, name=?, birthday=? WHERE user_id=?";
        int updated = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                id);

        if (updated == 0) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new FriendsAddingException("Пользователь не может добавить себя в друзья");
        }

        String checkSql = "SELECT COUNT(*) FROM user_friendships WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, friendId);

        if (count <= 0) {
            String insertSql = "INSERT INTO user_friendships (user_id, friend_id, confirmed) VALUES (?, ?, FALSE)";
            jdbcTemplate.update(insertSql, userId, friendId);
        }
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        String deleteSql = "DELETE FROM user_friendships WHERE (user_id = ? AND friend_id = ?)";
        jdbcTemplate.update(deleteSql, userId, friendId);
    }

    private void loadAllFriends(User user) {
        user.getFriends().clear();

        String sql = "SELECT friend_id FROM user_friendships WHERE user_id = ?";
        List<Long> friendIds = jdbcTemplate.queryForList(sql, Long.class, user.getId());

        for (Long fid : friendIds) {
            user.getFriends().put(fid, FriendshipStatus.CONFIRMED);
        }
    }
}