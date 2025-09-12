package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Component("inMemoryUserStorage")
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private final Map<Long, Set<Long>> friendships = new HashMap<>(); // userId -> set of friendIds

    private Long getNextId() {
        return users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0L) + 1;
    }

    @Override
    public User add(User user) {
        user.setId(getNextId());

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }

        users.put(user.getId(), user);
        log.debug("Добавлен пользователь с ID {}", user.getId());

        return user;
    }

    @Override
    public User update(User user) {
        User existingUser = users.get(user.getId());

        if (existingUser != null) {
            if (user.getName() == null || user.getName().isBlank()) {
                user.setName(user.getLogin());
            }

            Set<Long> currentFriends = friendships.get(user.getId());
            user.setFriends(currentFriends != null ? new HashSet<>(currentFriends) : new HashSet<>());

            users.put(user.getId(), user);
            log.debug("Обновлен пользователь с ID {}", user.getId());
        }

        return user;
    }

    @Override
    public Collection<User> findAll() {
        return users.values().stream()
                .map(this::enrichUserData)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> findById(Long id) {
        User user = users.get(id);
        if (user != null) {
            user = enrichUserData(user);
        }
        return Optional.ofNullable(user);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        friendships.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);

        User user = users.get(userId);
        if (user != null) {
            if (user.getFriends() == null) {
                user.setFriends(new HashSet<>());
            }
            user.getFriends().add(friendId);
        }

        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        Set<Long> userFriends = friendships.get(userId);

        if (userFriends != null && userFriends.remove(friendId)) {
            User user = users.get(userId);
            if (user != null && user.getFriends() != null) {
                user.getFriends().remove(friendId);
            }

            log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);

            if (userFriends.isEmpty()) {
                friendships.remove(userId);
            }
        } else {
            log.warn("Дружба между пользователями {} и {} не найдена", userId, friendId);
        }
    }

    @Override
    public List<User> getUserFriends(Long userId) {
        Set<Long> friendIds = friendships.getOrDefault(userId, new HashSet<>());

        return friendIds.stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .map(this::enrichUserData)
                .sorted(Comparator.comparing(User::getName,
                        Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        Set<Long> userFriends = friendships.getOrDefault(userId, new HashSet<>());
        Set<Long> otherUserFriends = friendships.getOrDefault(otherUserId, new HashSet<>());

        return userFriends.stream()
                .filter(otherUserFriends::contains)
                .map(users::get)
                .filter(Objects::nonNull)
                .map(this::enrichUserData)
                .sorted(Comparator.comparing(User::getName,
                        Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
    }

    private User enrichUserData(User user) {
        Set<Long> userFriendsIds = friendships.getOrDefault(user.getId(), new HashSet<>());

        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }

        user.setFriends(new HashSet<>(userFriendsIds));

        return user;
    }
}