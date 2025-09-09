package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        Collection<User> result = users.values();
        return result;
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        user.setId(getNextId());

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User updatedUser) {
        if (!users.containsKey(updatedUser.getId())) {
            log.warn("Попытка обновления несуществующего пользователя с ID: {}", updatedUser.getId());
            throw new NotFoundException("Нет пользователя с таким id");
        }

        User oldUser = users.get(updatedUser.getId());
        oldUser.setName(updatedUser.getName());
        oldUser.setEmail(updatedUser.getEmail());
        oldUser.setLogin(updatedUser.getLogin());

        if (updatedUser.getBirthday() != null) {
            oldUser.setBirthday(updatedUser.getBirthday());
        }

        log.info("Пользователь с ID: {} успешно обновлен", oldUser.getId());
        return oldUser;
    }

    private int getNextId() {
        int currentMaxId = users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
