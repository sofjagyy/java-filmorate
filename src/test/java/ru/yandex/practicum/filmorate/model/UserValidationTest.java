package ru.yandex.practicum.filmorate.model;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class UserValidationTest {
        private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        private static User validUser() {
            User user = User.builder()
                    .id(1)
                    .email("test@example.com")
                    .login("testuser")
                    .name("Test User")
                    .birthday(LocalDate.of(1990, 1, 1))
                    .build();
            return user;
        }


        @Test
        void userEmailNotNull() {
            User user = validUser();
            user.setEmail(null);

            Set<ConstraintViolation<User>> violations = validator.validate(user);

            assertEquals(1, violations.size());
            assertEquals("Электронная почта должна быть проинициализирована",
                    violations.iterator().next().getMessage());
        }

        @Test
        void userEmailHasCorrectFormat() {
            User user = validUser();
            user.setEmail("invalid-email.ru");

            Set<ConstraintViolation<User>> violations = validator.validate(user);

            assertEquals(1, violations.size());
            assertEquals("Электронная почта должна соответствовать формату и не может быть пустой",
                    violations.iterator().next().getMessage());
        }

        @Test
        void userLoginNotNull() {
            User user = validUser();
            user.setLogin(null);

            Set<ConstraintViolation<User>> violations = validator.validate(user);

            assertEquals(1, violations.size());
            assertEquals("Логин не Null",
                    violations.iterator().next().getMessage());
        }

        @Test
        void userLoginNotBlank() {
            User user = validUser();
            user.setLogin("  ");

            Set<ConstraintViolation<User>> violations = validator.validate(user);

            assertEquals(1, violations.size());
            assertEquals("Логин не может содержать пробелов или быть пустым",
                    violations.iterator().next().getMessage());
        }

        @Test
        void userLoginCantBeInTheFuture() {
            User user = validUser();
            user.setBirthday(LocalDate.of(2030, 1, 1));

            Set<ConstraintViolation<User>> violations = validator.validate(user);

            assertEquals(1, violations.size());
            assertEquals("День рождения не может быть в будущем",
                    violations.iterator().next().getMessage());
        }
    }





