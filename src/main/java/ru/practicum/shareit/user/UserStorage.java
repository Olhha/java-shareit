package ru.practicum.shareit.user;

import java.util.List;

public interface UserStorage {
    List<User> getAllUsers();

    User addUser(User user);

    boolean ifEmailIsNotUnique(String email, Integer userID);

    boolean ifUserExists(User user);

    User updateUser(User user);

    User getUserByID(int userID);

    User deleteUserByID(int userID);
}
