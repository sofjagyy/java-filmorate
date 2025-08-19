package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();

    private int getNextId() {
         int currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Запрос списка всех фильмов. Количество фильмов в базе: {}", films.size());
        Collection<Film> result = films.values();
        log.debug("Возвращаем {} фильмов", result.size());
        return result;
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        film.setId(getNextId());
        log.debug("Присвоен ID: {}", film.getId());

        films.put(film.getId(), film);
        log.info("Фильм ID: {} успешно создан и добавлен в коллекцию, всего фильмов: {}",
                 film.getId(), films.size());
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film updatedFilm) {
        log.info("Попытка обновления фильма с ID: {}", updatedFilm.getId());
        if (!films.containsKey(updatedFilm.getId())) {
            log.warn("Попытка обновления несуществующего фильма с ID: {}", updatedFilm.getId());
            throw new NotFoundException("Нет фильма с таким id");
        }

        Film oldFilm = films.get(updatedFilm.getId());
        log.info("Фильм найден: '{}'. Начинаем обновление", oldFilm.getName());

        oldFilm.setName(updatedFilm.getName());
        log.info("Обновляем название");

        oldFilm.setReleaseDate(updatedFilm.getReleaseDate());
        log.info("Обновляем дату релиза");

        if (updatedFilm.getDescription() != null) {
            oldFilm.setDescription(updatedFilm.getDescription());
        }
        log.info("Обновляем описание");

        if (updatedFilm.getDuration() != null) {
            log.info("Обновляем длительность");
            oldFilm.setDuration(updatedFilm.getDuration());
        }

        log.info("Фильм с ID: {} успешно обновлен", oldFilm.getId());
        return oldFilm;
    }
}
