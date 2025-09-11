package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.impl.UserDbStorage;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class UserService {

    @Qualifier("userDbStorage")  // Используем DAO реализацию
    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }
    public User getUserById(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
    }

    public User addUser(User user) {
        return userStorage.add(user);
    }

    public User updateUser(User updatedUser) {
        User existingUser = getUserById(updatedUser.getId());

        if (updatedUser.getName() != null) {
            existingUser.setName(updatedUser.getName());
        }

        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setLogin(updatedUser.getLogin());

        if (updatedUser.getBirthday() != null) {
            existingUser.setBirthday(updatedUser.getBirthday());
        }

        return userStorage.update(existingUser);
    }

    public Collection<User> getAllUsers() {
        return userStorage.findAll();
    }

    public void addFriend(Long userId, Long friendId) {
        getUserById(userId);
        getUserById(friendId);

        // Кастим к конкретной реализации для доступа к методу addFriend
        UserDbStorage userDbStorage = (UserDbStorage) userStorage;
        userDbStorage.addFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        getUserById(userId);
        getUserById(friendId);

        // Кастим к конкретной реализации для доступа к методу removeFriend
        UserDbStorage userDbStorage = (UserDbStorage) userStorage;
        userDbStorage.removeFriend(userId, friendId);
    }

    public List<User> getUserFriends(Long userId) {
        getUserById(userId);

        // Кастим к конкретной реализации для доступа к методу getUserFriends
        UserDbStorage userDbStorage = (UserDbStorage) userStorage;
        return userDbStorage.getUserFriends(userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        getUserById(userId);
        getUserById(otherUserId);

        UserDbStorage userDbStorage = (UserDbStorage) userStorage;
        return userDbStorage.getCommonFriends(userId, otherUserId);
    }
}