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
        log.info("Запрос списка всех пользователей. Количество пользователей в базе: {}", users.size());
        Collection<User> result = users.values();
        log.debug("Возвращаем {} пользователей", result.size());
        return result;
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("Создание нового пользователя в базе: '{}'", user.getLogin());

        user.setId(getNextId());
        log.debug("Присвоен ID: {} пользователю '{}'", user.getId(), user.getLogin());

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        users.put(user.getId(), user);
        log.info("Пользователь '{}' ID: {} успешно создан и добавлен в базу, всего пользователей: {}",
                user.getLogin(), user.getId(), users.size());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User updatedUser) {
        log.info("Попытка обновления данных пользователя с ID: {}", updatedUser.getId());
        if (!users.containsKey(updatedUser.getId())) {
            log.warn("Попытка обновления несуществующего пользователя с ID: {}", updatedUser.getId());
            throw new NotFoundException("Нет пользователя с таким id");
        }

        User oldUser = users.get(updatedUser.getId());
        log.info("Пользователь найден: '{}'. Начинаем обновление", oldUser.getLogin());

        oldUser.setName(updatedUser.getName());
        log.info("Обновляем имя");

        oldUser.setEmail(updatedUser.getEmail());
        log.info("Обновляем электронную почту");

        oldUser.setLogin(updatedUser.getLogin());
        log.info("Обновляем логин");

        if (updatedUser.getBirthday() != null) {
            log.info("Обновляем день рождения пользователя");
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
