package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.impl.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.impl.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, FilmDbStorage.class})

class FilmorateApplicationTests {
	private final UserDbStorage userStorage;
	private final FilmDbStorage filmStorage;

	@Test
	public void testFindUserById() {
		User testUser = new User("testuser", "Test User", "test@example.com", LocalDate.of(1990, 12, 15));
		User savedUser = userStorage.add(testUser);

		Optional<User> userOptional = userStorage.findById(savedUser.getId());  // Используем реальный ID

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(user ->
						assertThat(user).hasFieldOrPropertyWithValue("id", savedUser.getId())  // Проверяем реальный ID
				);
	}

	@Test
	public void testAddUser() {
		User testUser = new User("testlogin", "Test Name", "test@example.com", LocalDate.of(1990, 12, 15));
		User savedUser = userStorage.add(testUser);

		assertThat(savedUser.getId()).isNotNull();
		assertThat(savedUser.getId()).isEqualTo(1L);

		assertThat(savedUser.getLogin()).isEqualTo("testlogin");
		assertThat(savedUser.getName()).isEqualTo("Test Name");
		assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
		assertThat(savedUser.getBirthday()).isEqualTo(LocalDate.of(1990, 12, 15));
	}

	@Test
	public void testUpdateUser() {
		User originalUser = new User("original", "Original Name", "original@example.com", LocalDate.of(1990, 1, 1));
		User savedUser = userStorage.add(originalUser);

		savedUser.setLogin("updated");
		savedUser.setName("Updated Name");
		savedUser.setEmail("updated@example.com");
		savedUser.setBirthday(LocalDate.of(1995, 12, 31));

		User updatedUser = userStorage.update(savedUser);

		assertThat(updatedUser.getLogin()).isEqualTo("updated");
		assertThat(updatedUser.getName()).isEqualTo("Updated Name");
		assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
		assertThat(updatedUser.getBirthday()).isEqualTo(LocalDate.of(1995, 12, 31));
	}

	@Test
	public void testAddFriend() {
		User user1 = new User("user1", "User One", "user1@example.com", LocalDate.of(1990, 1, 1));
		User user2 = new User("user2", "User Two", "user2@example.com", LocalDate.of(1991, 2, 2));

		User savedUser1 = userStorage.add(user1);
		User savedUser2 = userStorage.add(user2);

		userStorage.addFriend(savedUser1.getId(), savedUser2.getId());

		List<User> user1Friends = userStorage.getUserFriends(savedUser1.getId());

		assertThat(user1Friends.size() == 1);
		assertThat(user1Friends.get(0).getId()).isEqualTo(savedUser2.getId());
		assertThat(user1Friends.get(0).getLogin()).isEqualTo("user2");
	}

	@Test
	public void testAddFilm() {
		Mpa mpa = new Mpa();
		mpa.setId(1L);
		mpa.setName("G");

		Film testFilm = new Film("Test Film", "Test Description", LocalDate.of(2023, 1, 1), 120, mpa);

		Film savedFilm = filmStorage.add(testFilm);

		assertThat(savedFilm.getId()).isNotNull();
		assertThat(savedFilm.getName()).isEqualTo("Test Film");
		assertThat(savedFilm.getDescription()).isEqualTo("Test Description");
		assertThat(savedFilm.getReleaseDate()).isEqualTo(LocalDate.of(2023, 1, 1));
		assertThat(savedFilm.getDuration()).isEqualTo(120);
		assertThat(savedFilm.getMpa().getId()).isEqualTo(1L);
	}

	@Test
	public void testUpdateFilm() {
		Mpa mpa = new Mpa();
		mpa.setId(1L);

		Film originalFilm = new Film("Original Film", "Original Description", LocalDate.of(2020, 1, 1), 90, mpa);
		Film savedFilm = filmStorage.add(originalFilm);

		Mpa newMpa = new Mpa();
		newMpa.setId(2L);

		savedFilm.setName("Updated Film");
		savedFilm.setDescription("Updated Description");
		savedFilm.setReleaseDate(LocalDate.of(2023, 12, 31));
		savedFilm.setDuration(150);
		savedFilm.setMpa(newMpa);

		Film updatedFilm = filmStorage.update(savedFilm);

		assertThat(updatedFilm.getName()).isEqualTo("Updated Film");
		assertThat(updatedFilm.getDescription()).isEqualTo("Updated Description");
		assertThat(updatedFilm.getReleaseDate()).isEqualTo(LocalDate.of(2023, 12, 31));
		assertThat(updatedFilm.getDuration()).isEqualTo(150);
		assertThat(updatedFilm.getMpa().getId()).isEqualTo(2L);
	}
}