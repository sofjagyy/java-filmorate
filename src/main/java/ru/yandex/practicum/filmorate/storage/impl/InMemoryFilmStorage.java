package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private final Map<Long, Set<Long>> filmLikes = new HashMap<>(); // filmId -> set of userIds

    private Long getNextId() {
        return films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0L) + 1;
    }

    @Override
    public Film add(Film film) {
        film.setId(getNextId());

        if (film.getGenres() == null) {
            film.setGenres(new LinkedHashSet<>());
        }

        film.setRate(0);

        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        Film existingFilm = films.get(film.getId());

        if (existingFilm != null) {
            Set<Long> currentLikes = filmLikes.get(film.getId());
            film.setRate(currentLikes != null ? currentLikes.size() : 0);
        }

        if (film.getGenres() == null) {
            film.setGenres(new LinkedHashSet<>());
        }

        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Collection<Film> findAll() {
        return films.values().stream()
                .map(this::enrichFilmData)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Film> findById(Long id) {
        Film film = films.get(id);
        if (film != null) {
            film = enrichFilmData(film);
        }
        return Optional.ofNullable(film);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        filmLikes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);

        Film film = films.get(filmId);
        if (film != null) {
            film.setRate(filmLikes.get(filmId).size());
        }

        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        Set<Long> likes = filmLikes.get(filmId);
        if (likes != null && likes.remove(userId)) {
            Film film = films.get(filmId);
            if (film != null) {
                film.setRate(likes.size());
            }

            log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);

            if (likes.isEmpty()) {
                filmLikes.remove(filmId);
            }
        }
    }

    @Override
    public List<Film> getPopularFilms(Integer count) {
        return films.values().stream()
                .map(this::enrichFilmData)
                .sorted((f1, f2) -> {
                    int likesCompare = Integer.compare(
                            f2.getRate() != null ? f2.getRate() : 0,
                            f1.getRate() != null ? f1.getRate() : 0
                    );
                    return likesCompare != 0 ? likesCompare : Long.compare(f1.getId(), f2.getId());
                })
                .limit(count)
                .collect(Collectors.toList());
    }

    private Film enrichFilmData(Film film) {
        Set<Long> likes = filmLikes.get(film.getId());
        film.setRate(likes != null ? likes.size() : 0);

        if (film.getGenres() == null) {
            film.setGenres(new LinkedHashSet<>());
        }

        return film;
    }
}