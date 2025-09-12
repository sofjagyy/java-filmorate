package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

@Repository("filmDbStorage")
@Primary
@RequiredArgsConstructor
@Slf4j
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film add(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"film_id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setLong(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        Long filmId = keyHolder.getKey().longValue();
        film.setId(filmId);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            updateFilmGenres(filmId, film.getGenres());
        }

        enrichSingleFilmData(film);
        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, " +
                "duration = ?, mpa_id = ? WHERE film_id = ?";

        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (film.getGenres() != null) {
            updateFilmGenres(film.getId(), film.getGenres());
        }

        enrichSingleFilmData(film);
        return film;
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, " +
                "m.mpa_id, m.name as mpa_name, m.description as mpa_description " +
                "FROM films f LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " +
                "ORDER BY f.film_id";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper());

        enrichFilmsDataBatch(films);
        return films;
    }

    @Override
    public Optional<Film> findById(Long id) {
        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, " +
                "m.mpa_id, m.name as mpa_name, m.description as mpa_description " +
                "FROM films f LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " +
                "WHERE f.film_id = ?";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper(), id);

        if (films.isEmpty()) {
            return Optional.empty();
        }

        Film film = films.get(0);
        enrichSingleFilmData(film);
        return Optional.of(film);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = "MERGE INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        int deletedRows = jdbcTemplate.update(sql, filmId, userId);
        if (deletedRows > 0) {
            log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);
        }
    }

    @Override
    public List<Film> getPopularFilms(Integer count) {
        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, " +
                "m.mpa_id, m.name as mpa_name, m.description as mpa_description, " +
                "COUNT(fl.user_id) as likes_count " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " +
                "LEFT JOIN film_likes fl ON f.film_id = fl.film_id " +
                "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, " +
                "         m.mpa_id, m.name, m.description " +
                "ORDER BY likes_count DESC, f.film_id " +
                "LIMIT ?";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper(), count);

        enrichFilmsDataBatch(films);
        return films;
    }

    private void updateFilmGenres(Long filmId, Collection<Genre> genres) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", filmId);

        if (genres != null && !genres.isEmpty()) {
            String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            List<Object[]> batchArgs = genres.stream()
                    .map(genre -> new Object[]{filmId, genre.getId()})
                    .collect(Collectors.toList());

            jdbcTemplate.batchUpdate(sql, batchArgs);
        }
    }

    private void enrichFilmsDataBatch(List<Film> films) {
        if (films.isEmpty()) return;

        List<Long> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());

        Map<Long, Set<Genre>> filmsGenres = loadGenresForFilmsBatch(filmIds);
        Map<Long, Integer> filmsLikes = loadLikesForFilmsBatch(filmIds);

        films.forEach(film -> {
            film.setGenres(filmsGenres.getOrDefault(film.getId(), new LinkedHashSet<>()));
            film.setRate(filmsLikes.getOrDefault(film.getId(), 0));
        });
    }

    private void enrichSingleFilmData(Film film) {
        Set<Genre> genres = loadGenresForSingleFilm(film.getId());
        Integer likes = loadLikesForSingleFilm(film.getId());

        film.setGenres(genres);
        film.setRate(likes);
    }

    private Map<Long, Set<Genre>> loadGenresForFilmsBatch(List<Long> filmIds) {
        if (filmIds.isEmpty()) return new HashMap<>();

        String inClause = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = "SELECT fg.film_id, g.genre_id, g.name " +
                "FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id IN (" + inClause + ") " +
                "ORDER BY fg.film_id, g.genre_id";

        Map<Long, Set<Genre>> result = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            Long filmId = rs.getLong("film_id");
            Genre genre = new Genre(rs.getLong("genre_id"), rs.getString("name"));
            result.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
        }, filmIds.toArray());

        return result;
    }

    private Map<Long, Integer> loadLikesForFilmsBatch(List<Long> filmIds) {
        if (filmIds.isEmpty()) return new HashMap<>();

        String inClause = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = "SELECT film_id, COUNT(user_id) as likes_count " +
                "FROM film_likes " +
                "WHERE film_id IN (" + inClause + ") " +
                "GROUP BY film_id";

        Map<Long, Integer> result = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            Long filmId = rs.getLong("film_id");
            Integer likesCount = rs.getInt("likes_count");
            result.put(filmId, likesCount);
        }, filmIds.toArray());

        return result;
    }

    private Set<Genre> loadGenresForSingleFilm(Long filmId) {
        String sql = "SELECT g.genre_id, g.name " +
                "FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id = ? " +
                "ORDER BY g.genre_id";

        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) ->
                new Genre(rs.getLong("genre_id"), rs.getString("name")), filmId);

        return new LinkedHashSet<>(genres);
    }

    private Integer loadLikesForSingleFilm(Long filmId) {
        String sql = "SELECT COUNT(user_id) as likes_count FROM film_likes WHERE film_id = ?";

        Integer likes = jdbcTemplate.queryForObject(sql, Integer.class, filmId);
        return likes != null ? likes : 0;
    }

    private RowMapper<Film> filmRowMapper() {
        return (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getLong("film_id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));

            if (rs.getLong("mpa_id") != 0) {
                Mpa mpa = new Mpa();
                mpa.setId(rs.getLong("mpa_id"));
                mpa.setName(rs.getString("mpa_name"));
                mpa.setDescription(rs.getString("mpa_description"));
                film.setMpa(mpa);
            }

            return film;
        };
    }
}