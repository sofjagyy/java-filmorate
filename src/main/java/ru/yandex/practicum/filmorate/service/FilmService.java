package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film addFilm(Film film) {
        return filmStorage.add(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.update(film);
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.findAll();
    }

    public Film getFilmById(Long id) {
        return filmStorage.findById(id);
    }

    public void addLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId);

        userStorage.findById(userId);

        if (film.getLikes() == null) {
            film.setLikes(new java.util.HashSet<>());
        }

        boolean wasAdded = film.getLikes().add(userId);
        if (wasAdded) {
            filmStorage.update(film);
            log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
        } else {
            log.info("Пользователь {} уже ставил лайк фильму {}", userId, filmId);
        }
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId);

        userStorage.findById(userId);

        if (film.getLikes() != null && film.getLikes().remove(userId)) {
            filmStorage.update(film);
            log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);
        } else {
            log.warn("Пользователь {} не ставил лайк фильму {}", userId, filmId);
        }
    }

    public List<Film> getPopularFilms(Integer count) {
        if (count == null || count <= 0) {
            count = 10;
        }

        Collection<Film> allFilms = filmStorage.findAll();

        return allFilms.stream()
                .sorted(Comparator.<Film>comparingInt(film ->
                        film.getLikes() != null ? film.getLikes().size() : 0).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }
}