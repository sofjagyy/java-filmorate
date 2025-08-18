package ru.yandex.practicum.filmorate.model;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FilmValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private static Film validFilm() {
        Film film = Film.builder()
                .id(1)
                .name("Тестовый фильм")
                .description("Описание")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();
        return film;
    }

    @Test
    void filmNameNotBlank() {
        Film film = validFilm();
        film.setName(" ");

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertEquals(1, violations.size());
        assertEquals("Название фильма не может быть пустым",
                violations.iterator().next().getMessage());
    }

    @Test
    void filmDescriptionLess200() {
        Film film = validFilm();
        film.setDescription("a".repeat(201));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertEquals(1, violations.size());
        assertEquals("Описание не может превышать 200 символов",
                violations.iterator().next().getMessage());
    }

    @Test
    void filmReleaseDateValidation() {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertEquals(1, violations.size());
        assertEquals("Фильмы существуют с 28 декабря 1895 года",
                violations.iterator().next().getMessage());
    }

    @Test
    void filmDurationIsPositive() {
        Film film = validFilm();
        film.setDuration(-10);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertEquals(1, violations.size());
        assertEquals("Длительность должна быть положительной",
                violations.iterator().next().getMessage());
    }
}
