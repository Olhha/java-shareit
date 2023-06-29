package ru.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;


import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;


@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class UserServiceImplUnitTest {
    @Mock
    UserRepository userRepositoryMock;
    @InjectMocks
    UserServiceImpl userServiceMock;
    User user;
    UserDto userDto;
    final Long userId = 0L;


    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(userId)
                .email("user@user.com")
                .name("Ivan Ivanov")
                .build();

        User user2 = new User();
        User user3 = new User();

        List<User> users = List.of(user, user2, user3);

        userDto = UserDto.builder()
                .id(userId)
                .email("userUpdated@user.com")
                .name("Name Updated")
                .build();

        Mockito.lenient().when(userRepositoryMock.save(Mockito.any()))
                .thenReturn(user);
        Mockito.lenient().when(userRepositoryMock.findAll())
                .thenReturn(users);
    }

    @Test
    void addUserTest() {
        UserDto userDto = UserDto.builder()
                .email("user@user.com")
                .name("Ivan Ivanov")
                .build();

        UserDto userAdded = userServiceMock.addUser(userDto);

        assertThat(userAdded, allOf(
                hasProperty("id", equalTo(user.getId())),
                hasProperty("email", equalTo(userDto.getEmail())),
                hasProperty("name", equalTo(userDto.getName()))
        ));
    }

    @Test
    void findAllUsers() {
        List<UserDto> usersDto = userServiceMock.findAllUsers();
        assertThat(usersDto, hasSize(3));
    }


    @Test
    void updateUser_allFields_test() {
        Mockito.lenient().when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(user));

        UserDto userUpdatedDto = userServiceMock.updateUser(userDto);

        assertThat(userUpdatedDto, allOf(
                hasProperty("id", equalTo(userDto.getId())),
                hasProperty("email", equalTo(userDto.getEmail())),
                hasProperty("name", equalTo(userDto.getName()))
        ));

    }

    @Test
    void updateUser_noUserForUpdate_test() {
        Mockito.lenient().when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userServiceMock.updateUser(userDto));
    }

    @Test
    void getUserByID_test() {
        Mockito.when(userRepositoryMock.findById(userId))
                .thenReturn(Optional.of(user));

        UserDto userFoundDto = userServiceMock.getUserByID(userId);

        assertThat(userFoundDto, allOf(
                hasProperty("id", equalTo(user.getId())),
                hasProperty("name", equalTo(user.getName())),
                hasProperty("email", equalTo(user.getEmail()))
        ));
    }

    @Test
    void getUserByID_notFound_test() {
        Mockito.when(userRepositoryMock.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userServiceMock.getUserByID(userId));
    }

    @Test
    void deleteUserByID_test() {
        Mockito.when(userRepositoryMock.findById(userId))
                .thenReturn(Optional.of(user));
        UserDto userDeletedDto = userServiceMock.deleteUserByID(userId);

        assertThat(userDeletedDto, allOf(
                hasProperty("id", equalTo(user.getId())),
                hasProperty("name", equalTo(user.getName())),
                hasProperty("email", equalTo(user.getEmail()))
        ));
    }

    @Test
    void deleteUserByID_notFound_test() {
        Mockito.when(userRepositoryMock.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userServiceMock.deleteUserByID(userId));
    }
}
