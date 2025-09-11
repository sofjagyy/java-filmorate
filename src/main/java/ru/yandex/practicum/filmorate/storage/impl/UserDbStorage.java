package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;

@Repository("userDbStorage")
@Primary// Имя для @Qualifier
@RequiredArgsConstructor
@Slf4j
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public User add(User user) {
        String sql = "INSERT INTO users (login, name, email, birthday) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"user_id"});
            ps.setString(1, user.getLogin());
            ps.setString(2, user.getName() != null && !user.getName().isBlank() ? user.getName() : user.getLogin());
            ps.setString(3, user.getEmail());
            ps.setDate(4, user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null);
            return ps;
        }, keyHolder);

        Long userId = keyHolder.getKey().longValue();
        user.setId(userId);

        // Устанавливаем имя как логин, если имя не задано
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET login = ?, name = ?, email = ?, birthday = ? WHERE user_id = ?";

        jdbcTemplate.update(sql,
                user.getLogin(),
                user.getName() != null && !user.getName().isBlank() ? user.getName() : user.getLogin(),
                user.getEmail(),
                user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null,
                user.getId());

        return findById(user.getId()).orElse(user);
    }

    @Override
    public Collection<User> findAll() {
        String sql = "SELECT user_id, login, name, email, birthday FROM users ORDER BY user_id";
        List<User> users = jdbcTemplate.query(sql, userRowMapper());

        // Загружаем друзей для всех пользователей
        Map<Long, Set<Long>> usersFriends = loadFriendsForUsers(
                users.stream().map(User::getId).toList()
        );

        users.forEach(user -> {
            user.setFriends(usersFriends.getOrDefault(user.getId(), new HashSet<>()));
        });

        return users;
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT user_id, login, name, email, birthday FROM users WHERE user_id = ?";

        List<User> users = jdbcTemplate.query(sql, userRowMapper(), id);

        if (users.isEmpty()) {
            return Optional.empty();
        }

        User user = users.get(0);
        user.setFriends(new HashSet<>(getUserFriendsIds(id)));

        return Optional.of(user);
    }

    public void addFriend(Long userId, Long friendId) {
        String sql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'UNCONFIRMED') " +
                "ON CONFLICT (user_id, friend_id) DO NOTHING";
        jdbcTemplate.update(sql, userId, friendId);
        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        int deletedRows = jdbcTemplate.update(sql, userId, friendId);

        if (deletedRows > 0) {
            log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
        } else {
            log.warn("Дружба между пользователями {} и {} не найдена", userId, friendId);
        }
    }

    public List<User> getUserFriends(Long userId) {
        String sql = "SELECT u.user_id, u.login, u.name, u.email, u.birthday " +
                "FROM users u " +
                "JOIN friendships f ON u.user_id = f.friend_id " +
                "WHERE f.user_id = ? " +
                "ORDER BY u.name";

        return jdbcTemplate.query(sql, userRowMapper(), userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        String sql = "SELECT u.user_id, u.login, u.name, u.email, u.birthday " +
                "FROM users u " +
                "JOIN friendships f1 ON u.user_id = f1.friend_id " +
                "JOIN friendships f2 ON u.user_id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ? " +
                "ORDER BY u.name";

        return jdbcTemplate.query(sql, userRowMapper(), userId, otherUserId);
    }

    private List<Long> getUserFriendsIds(Long userId) {
        String sql = "SELECT friend_id FROM friendships WHERE user_id = ?";
        return jdbcTemplate.queryForList(sql, Long.class, userId);
    }

    private Map<Long, Set<Long>> loadFriendsForUsers(List<Long> userIds) {
        if (userIds.isEmpty()) return new HashMap<>();

        String inClause = String.join(",", Collections.nCopies(userIds.size(), "?"));
        String sql = "SELECT user_id, friend_id FROM friendships WHERE user_id IN (" + inClause + ")";

        Map<Long, Set<Long>> result = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            Long userId = rs.getLong("user_id");
            Long friendId = rs.getLong("friend_id");
            result.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        }, userIds.toArray());

        return result;
    }

    private RowMapper<User> userRowMapper() {
        return (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getLong("user_id"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setEmail(rs.getString("email"));

            Date birthday = rs.getDate("birthday");
            if (birthday != null) {
                user.setBirthday(birthday.toLocalDate());
            }

            return user;
        };
    }
}