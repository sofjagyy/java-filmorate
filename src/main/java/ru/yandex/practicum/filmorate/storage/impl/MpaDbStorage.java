package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Mpa> findAll() {
        String sql = "SELECT mpa_id, name, description FROM mpa_ratings ORDER BY mpa_id";
        log.debug("Выполняется запрос: {}", sql);
        return jdbcTemplate.query(sql, mpaRowMapper());
    }

    @Override
    public Optional<Mpa> findById(Long id) {
        String sql = "SELECT mpa_id, name, description FROM mpa_ratings WHERE mpa_id = ?";
        log.debug("Выполняется запрос: {} с параметром id={}", sql, id);

        List<Mpa> mpaList = jdbcTemplate.query(sql, mpaRowMapper(), id);

        return mpaList.isEmpty() ? Optional.empty() : Optional.of(mpaList.get(0));
    }

    private RowMapper<Mpa> mpaRowMapper() {
        return (rs, rowNum) -> {
            Mpa mpa = new Mpa();
            mpa.setId(rs.getLong("mpa_id"));
            mpa.setName(rs.getString("name"));
            mpa.setDescription(rs.getString("description"));
            return mpa;
        };
    }
}