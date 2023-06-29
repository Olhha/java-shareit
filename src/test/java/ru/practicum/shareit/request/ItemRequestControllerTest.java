package ru.practicum.shareit.request;

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
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemRequestControllerTest {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;
    @MockBean
    ItemRequestService itemRequestService;

    ItemRequestResponseDto itemRequestResponseDto;
    ItemRequestDto itemRequestDto;
    ItemDto item;
    final long userId = 0L;
    final long requestId = 0L;

    @BeforeEach
    void setUp() {
        long itemId = 0L;
        item = ItemDto.builder()
                .id(itemId)
                .build();

        itemRequestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("Description")
                .requesterId(userId)
                .build();

        long responseId = 0L;
        itemRequestResponseDto = ItemRequestResponseDto.builder()
                .id(responseId)
                .description("Description")
                .requesterId(userId)
                .created(LocalDateTime.now())
                .items(List.of(item))
                .build();
    }

    @SneakyThrows
    @Test
    void addRequest() {
        Mockito.when(itemRequestService.addRequest(any(ItemRequestDto.class))).thenReturn(itemRequestResponseDto);
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestResponseDto.getDescription())))
                .andExpect(jsonPath("$.requesterId", is(itemRequestResponseDto.getRequesterId()), Long.class));
    }

    @SneakyThrows
    @Test
    void getRequestsForUser() {
        Mockito.when(itemRequestService.getRequestsForUser(anyLong())).thenReturn(List.of(itemRequestResponseDto));
        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemRequestResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestResponseDto.getDescription())))
                .andExpect(jsonPath("$[0].items", hasSize(1)))
                .andExpect(jsonPath("$[0].items[0].id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$[0].items[0].requestId", is(item.getRequestId()), Long.class));
    }

    @SneakyThrows
    @Test
    void getAllRequests() {
        Mockito.when(itemRequestService.getAllRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(itemRequestResponseDto));
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemRequestResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestResponseDto.getDescription())))
                .andExpect(jsonPath("$[0].items", hasSize(1)))
                .andExpect(jsonPath("$[0].items[0].id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$[0].items[0].requestId", is(item.getRequestId()), Long.class));

    }

    @SneakyThrows
    @Test
    void getRequestById() {
        Mockito.when(itemRequestService.getRequestById(anyLong(), anyLong()))
                .thenReturn(itemRequestResponseDto);
        mockMvc.perform(get("/requests/" + requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestResponseDto.getDescription())))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$.items[0].requestId", is(item.getRequestId()), Long.class));
    }
}