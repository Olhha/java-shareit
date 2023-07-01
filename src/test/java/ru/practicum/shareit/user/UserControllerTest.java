package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
@FieldDefaults(level = AccessLevel.PRIVATE)
class UserControllerTest {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;
    @MockBean
    UserService userService;
    UserDto userDto;
    UserDto userDto2;
    long userId;
    List<UserDto> userDtos;

    @BeforeEach
    void setUp() {
        userId = 0L;
        long userId2 = 2L;

        userDto = UserDto.builder()
                .id(userId)
                .name("Ivanov Ivan")
                .email("ivanov.ivan@email.com")
                .build();

        userDto2 = UserDto.builder()
                .id(userId2)
                .name("Ivanov Ivan2")
                .email("ivanov.ivan2@email.com")
                .build();

        userDtos = List.of(userDto, userDto2);
    }

    @SneakyThrows
    @Test
    void findAllUsers() {
        Mockito.when(userService.findAllUsers()).thenReturn(userDtos);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(userDto.getName())))
                .andExpect(jsonPath("$[0].email", is(userDto.getEmail())))
                .andExpect(jsonPath("$[1].id", is(userDto2.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(userDto2.getName())))
                .andExpect(jsonPath("$[1].email", is(userDto2.getEmail())));
    }

    @SneakyThrows
    @Test
    void addUser() {
        Mockito.when(userService.addUser(any(UserDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        Mockito.verify(userService).addUser(userDto);
    }

    @SneakyThrows
    @Test
    void addUser_emptyName() {
        Mockito.when(userService.addUser(any(UserDto.class))).thenReturn(userDto);

        UserDto incorrectUserDto = UserDto.builder()
                .email("e.e@email.com")
                .build();

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(incorrectUserDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.error", notNullValue()));
    }

    @SneakyThrows
    @Test
    void addUser_incorrectEmail() {
        Mockito.when(userService.addUser(any(UserDto.class))).thenReturn(userDto);

        UserDto incorrectUserDto = UserDto.builder()
                .email("e.email.com")
                .name("Ivanov Ivan")
                .build();

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(incorrectUserDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.error", notNullValue()));
    }

    @SneakyThrows
    @Test
    void updateUser() {
        Mockito.when(userService.updateUser(any(UserDto.class))).thenReturn(userDto);

        mockMvc.perform(patch("/users/" + userId)
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        Mockito.verify(userService).updateUser(userDto);
    }

    @SneakyThrows
    @Test
    void getUserById() {
        Mockito.when(userService.getUserByID(anyLong())).thenReturn(userDto);

        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        Mockito.verify(userService).getUserByID(userId);
    }

    @SneakyThrows
    @Test
    void deleteUserById() {
        Mockito.when(userService.deleteUserByID(anyLong())).thenReturn(userDto);

        mockMvc.perform(delete("/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        Mockito.verify(userService).deleteUserByID(userId);
    }
}