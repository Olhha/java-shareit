package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;

import java.util.*;

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

    @Override
    public User getUserByEmail(String email) {
        Optional<User> userOptional = users.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();

        return userOptional.orElse(null);
    }
}
