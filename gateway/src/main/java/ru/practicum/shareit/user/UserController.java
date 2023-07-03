package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.MarkerValidation;
import ru.practicum.shareit.user.dto.UserDto;


@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserClient userClient;

    @Autowired
    public UserController(UserClient userClient) {
        this.userClient = userClient;
    }

    @GetMapping
    public ResponseEntity<Object> findAllUsers() {
        return userClient.findAllUsers();
    }

    @PostMapping
    public ResponseEntity<Object> addUser(@Validated(MarkerValidation.OnCreate.class) @RequestBody UserDto userDto) {
        return userClient.addUser(userDto);
    }

    @PatchMapping("/{userID}")
    public ResponseEntity<Object> updateUser(@Validated(MarkerValidation.OnUpdate.class) @RequestBody UserDto userDto,
                                             @PathVariable long userID) {
        return userClient.updateUser(userDto.toBuilder().id(userID).build());
    }

    @GetMapping("/{userID}")
    public ResponseEntity<Object> getUserById(@PathVariable long userID) {
        return userClient.getUserByID(userID);
    }

    @DeleteMapping("/{userID}")
    public ResponseEntity<Object> deleteUserById(@PathVariable long userID) {
        return userClient.deleteUserByID(userID);
    }
}