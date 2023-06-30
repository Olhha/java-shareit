package ru.practicum.shareit.item;

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
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingLastNextDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UpdateForbiddenException;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithLastAndNextBookingsAndCommentsDto;

import javax.validation.ValidationException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemControllerTest {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;
    @MockBean
    ItemService itemService;
    ItemDto itemDto;
    ItemDto itemDto2;
    List<ItemDto> itemDtos;
    long itemId;
    long userId;
    ItemWithLastAndNextBookingsAndCommentsDto itemExtendedDto;
    CommentResponseDto commentResponseDto;

    @BeforeEach
    void setUp() {
        userId = 0L;
        itemId = 0L;
        long itemId2 = 2L;
        long requestId = 0L;

        itemDto = ItemDto.builder()
                .id(itemId)
                .name("Thing")
                .description("Useful thing")
                .available(true)
                .requestId(requestId)
                .build();

        itemDto2 = ItemDto.builder()
                .id(itemId2)
                .name("Thing2")
                .description("Useful thing2")
                .available(true)
                .requestId(requestId)
                .build();

        BookingLastNextDto bookingDto =
                BookingLastNextDto.builder()
                        .id(0L)
                        .start(LocalDateTime.now().plusDays(5))
                        .end(LocalDateTime.now().plusDays(7))
                        .itemId(itemId)
                        .bookerId(userId)
                        .status(Status.APPROVED.name())
                        .build();

        commentResponseDto =
                CommentResponseDto.builder()
                        .id(0L)
                        .text("good thing")
                        .authorName("Author1")
                        .created(LocalDateTime.now())
                        .build();

        List<CommentResponseDto> comments = List.of(commentResponseDto);

        itemExtendedDto =
                ItemWithLastAndNextBookingsAndCommentsDto.builder()
                        .id(itemId)
                        .name("Thing")
                        .description("Useful thing")
                        .available(true)
                        .comments(comments)
                        .lastBooking(bookingDto)
                        .nextBooking(bookingDto)
                        .build();

        itemDtos = List.of(itemDto, itemDto2);
    }

    @Test
    @SneakyThrows
    void addItem() {
        Mockito.when(itemService.addItem(any(ItemDto.class), anyLong())).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId().intValue())));
    }

    @Test
    @SneakyThrows
    void updateItem() {
        Mockito.when(itemService.updateItem(any(ItemDto.class), anyLong())).thenReturn(itemDto);

        mockMvc.perform(patch("/items/" + itemId)
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId().intValue())));
    }

    @Test
    @SneakyThrows
    void updateItem_byNotOwner() {
        Mockito.when(itemService.updateItem(any(ItemDto.class), anyLong()))
                .thenThrow(new UpdateForbiddenException("User isn't the owner of the item."));

        mockMvc.perform(patch("/items/" + itemId)
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", notNullValue()));
    }

    @Test
    @SneakyThrows
    void getItem() {
        Mockito.when(itemService.getItemByID(anyLong(), anyLong())).thenReturn(itemExtendedDto);
        mockMvc.perform(get("/items/" + itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemExtendedDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemExtendedDto.getName())))
                .andExpect(jsonPath("$.description", is(itemExtendedDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemExtendedDto.getAvailable())))
                .andExpect(jsonPath("$.lastBooking.id", is(itemExtendedDto.getLastBooking().getId()), Long.class))
                .andExpect(jsonPath("$.lastBooking.bookerId", is(itemExtendedDto.getLastBooking().getBookerId().intValue())))
                .andExpect(jsonPath("$.nextBooking.id", is(itemExtendedDto.getNextBooking().getId()), Long.class))
                .andExpect(jsonPath("$.nextBooking.bookerId", is(itemExtendedDto.getNextBooking().getBookerId().intValue())))
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].id", is(itemExtendedDto.getComments().get(0).getId()), Long.class))
                .andExpect(jsonPath("$.comments[0].text", is(itemExtendedDto.getComments().get(0).getText())))
                .andExpect(jsonPath("$.comments[0].authorName", is(itemExtendedDto.getComments().get(0).getAuthorName())));
    }

    @Test
    @SneakyThrows
    void getItem_itemNotFound() {
        Mockito.when(itemService.getItemByID(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("NotFoundException"));
        mockMvc.perform(get("/items/" + itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", notNullValue()));
    }

    @Test
    @SneakyThrows
    void getItemsForUser() {
        Mockito.when(itemService.getItemsForUser(anyLong(), anyInt(), anyInt())).thenReturn(List.of(itemExtendedDto));
        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemExtendedDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemExtendedDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemExtendedDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemExtendedDto.getAvailable())))
                .andExpect(jsonPath("$[0].lastBooking.id", is(itemExtendedDto.getLastBooking().getId()), Long.class))
                .andExpect(jsonPath("$[0].lastBooking.bookerId", is(itemExtendedDto.getLastBooking().getBookerId().intValue())))
                .andExpect(jsonPath("$[0].nextBooking.id", is(itemExtendedDto.getNextBooking().getId()), Long.class))
                .andExpect(jsonPath("$[0].nextBooking.bookerId", is(itemExtendedDto.getNextBooking().getBookerId().intValue())))
                .andExpect(jsonPath("$[0].comments", hasSize(1)))
                .andExpect(jsonPath("$[0].comments[0].id", is(itemExtendedDto.getComments().get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].comments[0].text", is(itemExtendedDto.getComments().get(0).getText())))
                .andExpect(jsonPath("$[0].comments[0].authorName", is(itemExtendedDto.getComments().get(0).getAuthorName())));
    }

    @Test
    @SneakyThrows
    void searchItemsByText() {
        Mockito.when(itemService.searchItemsByText(anyString(), anyInt(), anyInt())).thenReturn(itemDtos);
        mockMvc.perform(get("/items/search")
                        .param("text", "thing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(itemDto2.getId()), Long.class));
    }

    @Test
    @SneakyThrows
    void addCommentToItem() {
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setText("Very useful thing");
        Mockito.when(itemService.addCommentToItem(anyLong(), anyLong(), any(CommentRequestDto.class)))
                .thenReturn(commentResponseDto);
        mockMvc.perform(post("/items/" + itemId + "/comment")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(commentRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentResponseDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentResponseDto.getAuthorName())));
    }

    @Test
    @SneakyThrows
    void addCommentToItem_userCantComment() {
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setText("Very useful thing");
        Mockito.when(itemService.addCommentToItem(anyLong(), anyLong(), any(CommentRequestDto.class)))
                .thenThrow(new ValidationException("User can't comment on the Item"));
        mockMvc.perform(post("/items/" + itemId + "/comment")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(commentRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.error", notNullValue()));
    }
}