package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDto> findAllUsers() {
        return userService.findAllUsers();
    }

    @PostMapping
    public UserDto addUser(@Valid @RequestBody UserDto userDto) {
        return userService.addUser(userDto);
    }

    @PatchMapping("/{userID}")
    public UserDto updateUser(@Valid @RequestBody UserDto userDto,
                              @PathVariable int userID) {
        return userService.updateUser(userDto, userID);
    }

    @GetMapping("/{userID}")
    public UserDto getUserById(@PathVariable int userID) {
        return userService.getUserByID(userID);
    }

    @DeleteMapping("/{userID}")
    public UserDto deleteUserById(@PathVariable int userID) {
        return userService.deleteUserByID(userID);
    }
}