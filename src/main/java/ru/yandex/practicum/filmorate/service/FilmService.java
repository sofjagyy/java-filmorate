package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.impl.FilmDbStorage;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {

    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;

    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       GenreService genreService,
                       MpaService mpaService) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreService = genreService;
        this.mpaService = mpaService;
    }

    private final GenreService genreService;
    private final MpaService mpaService;

    public Film getFilmById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
    }

    private void checkUserExists(Long userId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }

    private void validateFilm(Film film) {

        if (film.getMpa() != null) {
            mpaService.getMpaRatingById(film.getMpa().getId());
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());

            Collection<Genre> existingGenres = genreService.getAllGenres();
            Set<Long> existingGenreIds = existingGenres.stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());

            Set<Long> invalidGenreIds = genreIds.stream()
                    .filter(id -> !existingGenreIds.contains(id))
                    .collect(Collectors.toSet());

            if (!invalidGenreIds.isEmpty()) {
                throw new NotFoundException("Жанры с ID " + invalidGenreIds + " не найдены");
            }
        }
    }

    public Film addFilm(Film film) {
        validateFilm(film);
        return filmStorage.add(film);
    }

    public Film updateFilm(Film updatedFilm) {
        Film existingFilm = getFilmById(updatedFilm.getId());
        validateFilm(updatedFilm);

        existingFilm.setName(updatedFilm.getName());
        existingFilm.setReleaseDate(updatedFilm.getReleaseDate());

        if (updatedFilm.getDescription() != null) {
            existingFilm.setDescription(updatedFilm.getDescription());
        }

        if (updatedFilm.getDuration() != null) {
            existingFilm.setDuration(updatedFilm.getDuration());
        }

        if (updatedFilm.getMpa() != null) {
            existingFilm.setMpa(updatedFilm.getMpa());
        }

        if (updatedFilm.getGenres() != null) {
            existingFilm.setGenres(updatedFilm.getGenres());
        }

        return filmStorage.update(existingFilm);
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.findAll();
    }

    public void addLike(Long filmId, Long userId) {
        getFilmById(filmId);
        checkUserExists(userId);

        FilmDbStorage filmDbStorage = (FilmDbStorage) filmStorage;
        filmDbStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        getFilmById(filmId);
        checkUserExists(userId);

        FilmDbStorage filmDbStorage = (FilmDbStorage) filmStorage;
        filmDbStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(Integer count) {
        if (count == null || count <= 0) {
            count = 10;
        }

        FilmDbStorage filmDbStorage = (FilmDbStorage) filmStorage;
        return filmDbStorage.getPopularFilms(count);
    }
}