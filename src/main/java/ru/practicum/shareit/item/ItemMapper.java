package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingLastNextDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithLastAndNextBookingsAndCommentsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestID(item.getRequest() != null ? item.getRequest().getId() : null)
                .build();
    }

    public static ItemWithLastAndNextBookingsAndCommentsDto toItemWithLastNextDatesAndCommentsDto(
            Item item, BookingLastNextDto lastBooking, BookingLastNextDto nextBooking,
            List<CommentResponseDto> commentsDto) {
        return ItemWithLastAndNextBookingsAndCommentsDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(commentsDto)
                .build();
    }

    public static Item toItem(ItemDto itemDto) {
        return Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .request(itemDto.getRequestID() != null ? getRequest(itemDto.getRequestID()) : null)
                .build();
    }

    private static ItemRequest getRequest(Long requestID) {
        return null;
    }
}
