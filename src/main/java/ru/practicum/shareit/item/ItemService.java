package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithLastAndNextBookingsAndCommentsDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(ItemDto itemDto, Long userID);

    ItemDto updateItem(ItemDto itemDto, Long userID);

    ItemWithLastAndNextBookingsAndCommentsDto getItemByID(long itemId, Long userID);

    List<ItemWithLastAndNextBookingsAndCommentsDto> getItemsForUser(Long userID);

    List<ItemDto> searchItemsByText(String text);

    CommentDto addCommentToItem(Long itemId, Long userID, CommentDto commentDto);
}
