package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@Slf4j
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
    public UserDto addUser(@RequestBody UserDto userDto) {
        log.info("Creating user {}", userDto);
        return userService.addUser(userDto);
    }

    @PatchMapping("/{userID}")
    public UserDto updateUser(@RequestBody UserDto userDto,
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