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
    Long id;
    String name;
    String description;
    Boolean available;
    BookingLastNextDto lastBooking;
    BookingLastNextDto nextBooking;
    List<CommentDto> comments;
}
