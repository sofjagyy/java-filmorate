package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;
import jakarta.validation.constraints.Email;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode(of = { "id" })
public class User {
    protected int id;

    @NotBlank(message = "Электронная почта должна быть проинициализирована")
    @Email(message = "Электронная почта должна соответствовать формату и не может быть пустой")
    protected String email;

    @NotNull(message = "Логин не Null")
    @Pattern(regexp = "^\\S+$", message = ("Логин не может содержать пробелов или быть пустым"))
    protected String login;

    protected String name;

    @PastOrPresent(message = "День рождения не может быть в будущем")
    protected LocalDate birthday;
}


