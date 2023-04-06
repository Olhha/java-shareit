package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("ItemStorageInMem")
public class ItemStorageInMem implements ItemStorage {

    private int itemID = 0;
    private final Map<Integer, Item> items = new HashMap<>();

    @Override
    public Item addItem(Item item) {
        int id = ++itemID;
        item.setId(id);
        items.put(id, item);
        return item;
    }

    @Override
    public Item getItemByID(int itemID) {
        return items.get(itemID);
    }

    @Override
    public Item updateItem(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public List<Item> getItemsForUser(Integer userID) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId() == userID)
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> searchItemsByText(String text) {
        return items.values().stream()
                .filter(item -> item.getAvailable().equals(true))
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .collect(Collectors.toList());
    }
}
