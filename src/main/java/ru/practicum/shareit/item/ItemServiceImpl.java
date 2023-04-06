package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.UpdateForbiddenException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserMapper;

import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserService userService;

    @Autowired
    public ItemServiceImpl(@Qualifier("ItemStorageInMem") ItemStorage itemStorage,
                           UserService userService) {
        this.itemStorage = itemStorage;
        this.userService = userService;
    }

    @Override
    public ItemDto addItem(ItemDto itemDto, Integer userID) {
        User user = UserMapper.toUser(userService.getUserByID(userID));
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(user);
        return ItemMapper.toItemDto(itemStorage.addItem(item));
    }

    @Override
    public ItemDto updateItem(int itemID, ItemDto itemDto, Integer userID) {
        Item item = itemStorage.getItemByID(itemID);

        String nameUpdate = itemDto.getName();
        String descriptionUpdate = itemDto.getDescription();
        Boolean availableUpdate = itemDto.getAvailable();

        validateOwner(userID, item);

        setUpdate(item, nameUpdate, descriptionUpdate, availableUpdate);

        return ItemMapper.toItemDto(itemStorage.updateItem(item));
    }

    private static void validateOwner(Integer userID, Item item) {
        if (userID == null) {
            throw new ValidationException("Owner id is empty.");
        }

        if (item.getOwner().getId() != userID) {
            throw new UpdateForbiddenException(String.format(
                    "User id = %d isn't the owner of item id = %d.", userID, item.getId()));
        }
    }

    private static void setUpdate(Item item, String nameUpdate, String descriptionUpdate, Boolean availableUpdate) {
        if (nameUpdate != null) {
            item.setName(nameUpdate);
        }

        if (descriptionUpdate != null) {
            item.setDescription(descriptionUpdate);
        }

        if (availableUpdate != null) {
            item.setAvailable(availableUpdate);
        }
    }

    @Override
    public ItemDto getItemByID(int itemId) {
        return ItemMapper.toItemDto(itemStorage.getItemByID(itemId));
    }

    @Override
    public List<ItemDto> getItemsForUser(Integer userID) {
        List<Item> items = itemStorage.getItemsForUser(userID);

        return itemsListToDtoList(items);
    }

    @Override
    public List<ItemDto> searchItemsByText(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        List<Item> itemsFound = itemStorage.searchItemsByText(text);

        return itemsListToDtoList(itemsFound);
    }

    private static List<ItemDto> itemsListToDtoList(List<Item> items) {
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
