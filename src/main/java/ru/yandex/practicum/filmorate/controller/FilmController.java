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
        Collection<Film> result = films.values();
        return result;
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film updatedFilm) {
        if (!films.containsKey(updatedFilm.getId())) {
            log.warn("Попытка обновления несуществующего фильма с ID: {}", updatedFilm.getId());
            throw new NotFoundException("Нет фильма с таким id");
        }

        Film oldFilm = films.get(updatedFilm.getId());

        oldFilm.setName(updatedFilm.getName());

        oldFilm.setReleaseDate(updatedFilm.getReleaseDate());

        if (updatedFilm.getDescription() != null) {
            oldFilm.setDescription(updatedFilm.getDescription());
        }

        if (updatedFilm.getDuration() != null) {
            oldFilm.setDuration(updatedFilm.getDuration());
        }
        log.info("Фильм с ID: {} успешно обновлен", oldFilm.getId());
        return oldFilm;
    }
}
