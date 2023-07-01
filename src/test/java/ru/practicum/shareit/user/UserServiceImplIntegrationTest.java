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

import javax.persistence.*;
import javax.transaction.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureTestDatabase
@FieldDefaults(level = AccessLevel.PRIVATE)
@Transactional
class UserServiceImplIntegrationTest {
    @Autowired
    EntityManager entityManager;
    @Autowired
    UserService userService;
    UserDto userDto;
    long userId;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .email("user@user.com")
                .name("Sidor Sidorov")
                .build();

        entityManager.createNativeQuery("INSERT INTO users (email, name) VALUES (?,?)")
                .setParameter(1, userDto.getEmail())
                .setParameter(2, userDto.getName())
                .executeUpdate();

        userId = entityManager.createQuery(
                        "SELECT u FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", userDto.getEmail())
                .getSingleResult().getId();
    }

    @AfterEach
    void cleanUp() {
        entityManager.createQuery("delete from User").executeUpdate();
    }

    @Test
    void addUser() {
        UserDto userSaved = UserDto.builder()
                .email("test@user.com")
                .name("Ivanov Test")
                .build();

        userService.addUser(userSaved);

        List<User> users = selectAllUsers();

        assertThat(users, hasSize(2));
        assertThat(users.get(1).getEmail(), equalTo(userSaved.getEmail()));
        assertThat(users.get(1).getName(), equalTo(userSaved.getName()));
    }

    @Test
    void findAllUsers() {
        List<UserDto> users = userService.findAllUsers();
        assertThat(users, hasSize(1));
        assertThat(users.get(0), allOf(
                hasProperty("email", equalTo(userDto.getEmail())),
                hasProperty("name", equalTo(userDto.getName()))
        ));
    }

    @Test
    void addUser_duplicateEmail() {
        UserDto newUser = UserDto.builder()
                .email("user@user.com")
                .name("New User")
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> userService.addUser(newUser));
        entityManager.clear();
    }

    @Test
    void updateUser() {
        UserDto newUserDto = UserDto.builder()
                .id(userId)
                .email("newEmail@user.com")
                .name("New Name")
                .build();

        userService.updateUser(newUserDto);

        List<User> users = selectAllUsers();
        assertThat(users, hasSize(1));
        assertThat(users.get(0).getName(), equalTo("New Name"));
    }

    @Test
    void getUserByID() {
        UserDto userById = userService.getUserByID(userId);
        assertThat(userById.getEmail(), equalTo(userDto.getEmail()));
        assertThat(userById.getName(), equalTo(userDto.getName()));
    }

    @Test
    void deleteUserByID() {
        userService.deleteUserByID(userId);
        assertThat(userService.findAllUsers(), hasSize(0));
    }

    private List<User> selectAllUsers() {
        return entityManager.createQuery("select u from User u",
                User.class).getResultList();
    }
}