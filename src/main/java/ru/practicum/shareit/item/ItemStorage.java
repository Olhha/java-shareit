package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item addItem(Item item);

    Item getItemByID(int itemID);

    Item updateItem(Item item);

    List<Item> getItemsForUser(Integer userID);

    List<Item> searchItemsByText(String text);
}
