package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    private Film getFilmByIdInternal(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
    }

    private void checkUserExists(Long userId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }

    public Film addFilm(Film film) {
        return filmStorage.add(film);
    }

    public Film updateFilm(Film updatedFilm) {
        Film existingFilm = getFilmByIdInternal(updatedFilm.getId());

        existingFilm.setName(updatedFilm.getName());
        existingFilm.setReleaseDate(updatedFilm.getReleaseDate());

        if (updatedFilm.getDescription() != null) {
            existingFilm.setDescription(updatedFilm.getDescription());
        }

        if (updatedFilm.getDuration() != null) {
            existingFilm.setDuration(updatedFilm.getDuration());
        }

        return filmStorage.update(existingFilm);
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.findAll();
    }

    public Film getFilmById(Long id) {
        return getFilmByIdInternal(id);
    }

    public void addLike(Long filmId, Long userId) {
        Film film = getFilmByIdInternal(filmId);
        checkUserExists(userId);

        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }

        boolean wasAdded = film.getLikes().add(userId);
        if (wasAdded) {
            filmStorage.update(film);
        } else {
            log.info("Пользователь {} уже ставил лайк фильму {}", userId, filmId);
        }
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = getFilmByIdInternal(filmId);
        checkUserExists(userId);

        if (film.getLikes() != null && film.getLikes().remove(userId)) {
            filmStorage.update(film);
        } else {
            log.warn("Пользователь {} не ставил лайк фильму {}", userId, filmId);
        }
    }

    public List<Film> getPopularFilms(Integer count) {
        if (count == null || count <= 0) {
            count = 10;
        }

        return filmStorage.findAll().stream()
                .sorted(Comparator.<Film>comparingInt(film ->
                        film.getLikes() != null ? film.getLikes().size() : 0).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }
}