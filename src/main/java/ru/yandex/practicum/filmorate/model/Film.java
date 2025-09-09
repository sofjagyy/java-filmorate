package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;
import ru.yandex.practicum.filmorate.validation.ValidFilmReleaseDate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = { "id" })
@Builder
public class Film {
    private Long id;

    @NotBlank(message = "Название фильма не может быть пустым")
    protected String name;

    @Size(max = 200, message = "Описание не может превышать 200 символов")
    protected String description;

    @ValidFilmReleaseDate(message = "Фильмы существуют с 28 декабря 1895 года")
    protected LocalDate releaseDate;

    @Positive(message = "Длительность должна быть положительной")
    private Integer duration;

    @Builder.Default
    private Set<Long> likes = new HashSet<>();
}