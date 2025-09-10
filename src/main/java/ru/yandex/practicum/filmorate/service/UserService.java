package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

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

        if (updatedUser.getFriends() != null) {
            existingUser.setFriends(updatedUser.getFriends());
        }

        return userStorage.update(existingUser);
    }

    public Collection<User> getAllUsers() {
        return userStorage.findAll();
    }

    public void addFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }

        if (friend.getFriends() == null) {
            friend.setFriends(new HashSet<>());
        }

        boolean userAdded = user.getFriends().add(friendId);
        boolean friendAdded = friend.getFriends().add(userId);

        if (userAdded) {
            userStorage.update(user);
        }

        if (friendAdded) {
            userStorage.update(friend);
        }
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        boolean userRemoved = false;
        boolean friendRemoved = false;

        if (user.getFriends() != null) {
            userRemoved = user.getFriends().remove(friendId);
        }

        if (friend.getFriends() != null) {
            friendRemoved = friend.getFriends().remove(userId);
        }

        if (userRemoved) {
            userStorage.update(user);
        }

        if (friendRemoved) {
            userStorage.update(friend);
        }

        if (!userRemoved && !friendRemoved) {
            log.warn("Пользователи {} и {} не являются друзьями", userId, friendId);
        }
    }

    public List<User> getUserFriends(Long userId) {
        User user = getUserById(userId);

        if (user.getFriends() == null || user.getFriends().isEmpty()) {
            return List.of();
        }

        return user.getFriends().stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        User user = getUserById(userId);
        User otherUser = getUserById(otherUserId);

        if (user.getFriends() == null || otherUser.getFriends() == null) {
            return List.of();
        }

        Set<Long> commonFriendIds = new HashSet<>(user.getFriends());
        commonFriendIds.retainAll(otherUser.getFriends());

        return commonFriendIds.stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }
}