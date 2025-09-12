package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository("GenreDbStorage")
@RequiredArgsConstructor
@Primary
@Slf4j
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Genre> findAll() {
        String sql = "SELECT genre_id, name FROM genres ORDER BY genre_id";
        log.debug("Выполняется запрос: {}", sql);
        return jdbcTemplate.query(sql, genreRowMapper());
    }

    @Override
    public Optional<Genre> findById(Long id) {
        String sql = "SELECT genre_id, name FROM genres WHERE genre_id = ?";
        log.debug("Выполняется запрос: {} с параметром id={}", sql, id);
        List<Genre> genres = jdbcTemplate.query(sql, genreRowMapper(), id);
        return genres.isEmpty() ? Optional.empty() : Optional.of(genres.get(0));
    }

    private RowMapper<Genre> genreRowMapper() {
        return (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getLong("genre_id"));
            genre.setName(rs.getString("name"));
            return genre;
        };
    }
}