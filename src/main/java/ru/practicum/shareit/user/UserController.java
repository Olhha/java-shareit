package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.MarkerValidation;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserServiceImpl userService;

    @Autowired
    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDto> findAllUsers() {
        return userService.findAllUsers();
    }

    @PostMapping
    @Validated(MarkerValidation.OnCreate.class)
    public UserDto addUser(@Valid @RequestBody UserDto userDto) {
        return userService.addUser(userDto);
    }

    @PatchMapping("/{userID}")
    public UserDto updateUser(@Valid @RequestBody UserDto userDto,
                              @PathVariable long userID) {
        return userService.updateUser(userDto.toBuilder().id(userID).build());
    }

    @GetMapping("/{userID}")
    public UserDto getUserById(@PathVariable long userID) {
        return userService.getUserByID(userID);
    }

    @DeleteMapping("/{userID}")
    public UserDto deleteUserById(@PathVariable long userID) {
        return userService.deleteUserByID(userID);
    }
}