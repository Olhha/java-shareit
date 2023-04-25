package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(ItemDto itemDto, Integer userID);

    ItemDto updateItem(ItemDto itemDto, Integer userID);

    ItemDto getItemByID(int itemId);

    List<ItemDto> getItemsForUser(Integer userID);

    List<ItemDto> searchItemsByText(String text);
}
