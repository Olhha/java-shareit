package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import javax.validation.ValidationException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("UserStorageInMem") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<UserDto> findAllUsers() {
        return userStorage.getAllUsers()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    public UserDto addUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        validateNewUser(user);
        return UserMapper.toUserDto(userStorage.addUser(user));
    }

    public UserDto updateUser(UserDto userDto, Integer userID) {
        User user = userStorage.getUserByID(userID);

        if (user == null) {
            throw new NotFoundException(String.format(
                    "User with id = %d doesn't exist.", userID));
        }

        String nameUpdate = userDto.getName();
        String emailUpdate = userDto.getEmail();

        if (emailUpdate != null) {
            validateUserEmail(emailUpdate, userID);
            user.setEmail(emailUpdate);
        }
        if (nameUpdate != null) {
            user.setName(nameUpdate);
        }

        return UserMapper.toUserDto(userStorage.updateUser(user));
    }

    public UserDto getUserByID(int userID) {
        User user = userStorage.getUserByID(userID);
        if (user == null) {
            throw new NotFoundException(String.format(
                    "User with id = %d doesn't exists.", userID));
        }
        return UserMapper.toUserDto(user);
    }

    private void validateNewUser(User user) {
        validateUserEmail(user.getEmail(), user.getId());

        if (userStorage.ifUserExists(user)) {
            throw new AlreadyExistException(String.format(
                    "User with id = %d already exists.", user.getId()));
        }
    }

    private void validateUserEmail(String email, Integer userID) {
        if (email == null) {
            throw new ValidationException("Email couldn't be empty.");
        }
        if (userStorage.ifEmailIsNotUnique(email, userID)) {
            throw new AlreadyExistException(String.format(
                    "User with Email = %s is already exists", email));
        }
    }

    public UserDto deleteUserByID(int userID) {
        //TODO: удалить все его вещи?
        User userDeleted = userStorage.deleteUserByID(userID);
        if (userDeleted == null) {
            throw new NotFoundException(String.format(
                    "User with id = %d doesn't exists.", userID));
        }
        return UserMapper.toUserDto(userDeleted);
    }
}
