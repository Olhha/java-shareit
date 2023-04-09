package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage {
    List<User> getAllUsers();

    User addUser(User user);

    User updateUser(User user);

    User getUserByID(int userID);

    User deleteUserByID(int userID);

    User getUserByEmail(String email);
}
