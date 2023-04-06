package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("UserStorageInMem")
public class UserStorageInMem implements UserStorage {
    private Integer userID = 0;
    private final Map<Integer, User> users = new HashMap<>();

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User addUser(User user) {
        Integer id = ++userID;
        user.setId(id);
        users.put(id, user);
        return user;
    }

    @Override
    public boolean ifEmailIsNotUnique(String email, Integer userID) {
        if (userID == null) {
            return users.values().stream().anyMatch(user -> user.getEmail().equals(email));
        }

        return users.values().stream().anyMatch(user -> user.getEmail().equals(email) &&
                user.getId() != userID);
    }

    @Override
    public boolean ifUserExists(User user) {
        return users.get(user.getId()) != null;
    }

    @Override
    public User updateUser(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User getUserByID(int userID) {
        return users.get(userID);
    }

    @Override
    public User deleteUserByID(int userID) {
        return users.remove(userID);
    }
}
