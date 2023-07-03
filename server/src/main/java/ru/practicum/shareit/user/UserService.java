package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> findAllUsers();

    UserDto addUser(UserDto userDto);

    UserDto updateUser(UserDto userDto);

    UserDto getUserByID(long userID);

    UserDto deleteUserByID(long userID);
}
