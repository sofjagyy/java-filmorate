package ru.yandex.practicum.filmorate.validation;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class FilmReleaseDateValidator implements ConstraintValidator<ValidFilmReleaseDate, LocalDate> {

    private static final LocalDate FIRST_MOVIE_DATE = LocalDate.of(1895, 12, 28);

    @Override
    public boolean isValid(LocalDate releaseDate, ConstraintValidatorContext context) {
        if (releaseDate == null) {
            return true;
        }

        return !releaseDate.isBefore(FIRST_MOVIE_DATE);
    }
}
