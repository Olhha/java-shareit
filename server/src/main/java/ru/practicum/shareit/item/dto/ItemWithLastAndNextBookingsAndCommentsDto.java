package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingLastNextDto;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ItemWithLastAndNextBookingsAndCommentsDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingLastNextDto lastBooking;
    private BookingLastNextDto nextBooking;
    private List<CommentResponseDto> comments;
}
