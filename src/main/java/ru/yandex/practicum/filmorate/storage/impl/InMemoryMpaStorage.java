package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.*;

@Component("InMemoryMpaStorage")
public class InMemoryMpaStorage implements MpaStorage {

    private final Map<Long, Mpa> mpaRatings = new HashMap<>();

    public InMemoryMpaStorage() {
        mpaRatings.put(1L, new Mpa(1L, "G", "У фильма нет возрастных ограничений"));
        mpaRatings.put(2L, new Mpa(2L, "PG", "Детям рекомендуется смотреть фильм с родителями"));
        mpaRatings.put(3L, new Mpa(3L, "PG-13", "Детям до 13 лет просмотр не желателен"));
        mpaRatings.put(4L, new Mpa(4L, "R", "Лицам до 17 лет просматривать фильм можно только в присутствии взрослого"));
        mpaRatings.put(5L, new Mpa(5L, "NC-17", "Лицам до 18 лет просмотр запрещён"));
    }

    @Override
    public Collection<Mpa> findAll() {
        return new ArrayList<>(mpaRatings.values());
    }

    @Override
    public Optional<Mpa> findById(Long id) {
        return Optional.ofNullable(mpaRatings.get(id));
    }
}