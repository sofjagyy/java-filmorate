package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import java.util.Collection;

public interface FilmStorage {
    Film add(Film film);

    Film update(Film film);

    Collection<Film> findAll();

    Film findById(Long id);
}