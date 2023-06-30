package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.dto.BookerDto;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
@FieldDefaults(level = AccessLevel.PRIVATE)
class BookingControllerTest {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;
    @MockBean
    BookingService bookingService;
    BookingRequestDto bookingRequestDto;
    private BookingResponseDto bookingResponseDto;
    final long userId = 0L;
    final long bookingId = 0L;

    @BeforeEach
    void setUp() {
        long itemId = 0L;
        bookingRequestDto = BookingRequestDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .bookerId(userId)
                .itemId(itemId)
                .build();

        bookingResponseDto = BookingResponseDto.builder()
                .id(bookingId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(5))
                .booker(new BookerDto(userId))
                .status(Status.WAITING.name())
                .item(new BookingItemDto(itemId, "Thing"))
                .build();
    }

    @SneakyThrows
    @Test
    void addBooking() {
        Mockito.when(bookingService.addBooking(any(BookingRequestDto.class)))
                .thenReturn(bookingResponseDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(bookingRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.id", is(bookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingResponseDto.getStatus())))
                .andExpect(jsonPath("$.booker.id", is(bookingResponseDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(bookingResponseDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(bookingResponseDto.getItem().getName())));
    }

    @SneakyThrows
    @Test
    void approveBooking() {
        Mockito.when(bookingService.approveBooking(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(bookingResponseDto);
        mockMvc.perform(patch("/bookings/" + bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingResponseDto.getStatus())))
                .andExpect(jsonPath("$.booker.id", is(bookingResponseDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(bookingResponseDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(bookingResponseDto.getItem().getName())));
    }

    @SneakyThrows
    @Test
    void getBookingById() {
        Mockito.when(bookingService.getBookingByIdByOwnerOrBooker(anyLong(), anyLong()))
                .thenReturn(bookingResponseDto);
        mockMvc.perform(get("/bookings/" + bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingResponseDto.getStatus())))
                .andExpect(jsonPath("$.booker.id", is(bookingResponseDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(bookingResponseDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(bookingResponseDto.getItem().getName())));
    }

    @SneakyThrows
    @Test
    void getAllBookingsForUser() {
        Mockito.when(bookingService.getAllBookingsForUser(anyLong(), anyString(),
                anyInt(), anyInt())).thenReturn(List.of(bookingResponseDto));
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is(bookingResponseDto.getStatus())))
                .andExpect(jsonPath("$[0].booker.id", is(bookingResponseDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(bookingResponseDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(bookingResponseDto.getItem().getName())));
    }

    @SneakyThrows
    @Test
    void getAllBookingsForOwner() {
        Mockito.when(bookingService.getAllBookingsForOwner(anyLong(), anyString(),
                anyInt(), anyInt())).thenReturn(List.of(bookingResponseDto));
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", "1")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is(bookingResponseDto.getStatus())))
                .andExpect(jsonPath("$[0].booker.id", is(bookingResponseDto.getBooker().getId()),Long.class))
                .andExpect(jsonPath("$[0].item.id", is(bookingResponseDto.getItem().getId()),Long.class))
                .andExpect(jsonPath("$[0].item.name", is(bookingResponseDto.getItem().getName())));
    }
}