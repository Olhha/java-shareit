package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage {
    List<User> getAllUsers();

    User addUser(User user);

    boolean ifEmailIsNotUnique(String email, Integer userID);

    User updateUser(User user);

    User getUserByID(int userID);

    User deleteUserByID(int userID);
}
