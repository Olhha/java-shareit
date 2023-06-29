package ru.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureTestDatabase
@FieldDefaults(level = AccessLevel.PRIVATE)
class UserServiceImplIntegrTest {
    @Autowired
    EntityManager entityManager;
    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;

    User user;
    UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .email("user@user.com")
                .name("Sidor Sidorov")
                .build();

        userDto = userService.addUser(userDto);
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void findAllUsers() {
        assertThat(userService.findAllUsers(), hasSize(1));
    }

    @Test
    void addUser_test() {
        assertThat(userService.findAllUsers(), hasSize(1));
    }

    @Test
    void addUser_duplicateEmail_test() {
        UserDto newUser = UserDto.builder()
                .email("user@user.com")
                .name("New User")
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> userService.addUser(newUser));
    }

    @Test
    void updateUser() {
        UserDto newUserDto = UserDto.builder()
                .id(userDto.getId())
                .email("newEmail@user.com")
                .name("New Name")
                .build();

        userService.updateUser(newUserDto);

        List<UserDto> users = userService.findAllUsers();
        assertThat(users.get(0).getName(), equalTo("New Name"));
    }

    @Test
    void getUserByID() {
        UserDto userById = userService.getUserByID(userDto.getId());
        assertThat(userById.getEmail(), equalTo(userDto.getEmail()));
        assertThat(userById.getName(), equalTo(userDto.getName()));
    }

    @Test
    void deleteUserByID() {
        userService.deleteUserByID(userDto.getId());
        assertThat(userService.findAllUsers(), hasSize(0));
    }
}