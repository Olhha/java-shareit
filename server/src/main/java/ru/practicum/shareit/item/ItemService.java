package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithLastAndNextBookingsAndCommentsDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(ItemDto itemDto, Long userID);

    ItemDto updateItem(ItemDto itemDto, Long userID);

    ItemWithLastAndNextBookingsAndCommentsDto getItemByID(long itemId, Long userID);

    List<ItemWithLastAndNextBookingsAndCommentsDto> getItemsForUser(
            Long userID, Integer from, Integer size);

    List<ItemDto> searchItemsByText(String text, Integer from, Integer size);

    CommentResponseDto addCommentToItem(Long itemId, Long userID, CommentRequestDto commentRequestDto);
}
