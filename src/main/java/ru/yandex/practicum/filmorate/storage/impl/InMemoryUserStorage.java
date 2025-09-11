package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Component("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

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
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public Optional<User> findById(Long id) {
        User user = users.get(id);
        if (user != null && user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        return Optional.ofNullable(user);
    }
}