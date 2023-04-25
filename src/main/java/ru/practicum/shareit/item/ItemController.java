package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.MarkerValidation;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    @Validated({MarkerValidation.OnCreate.class})
    public ItemDto addItem(@RequestHeader(USER_ID_HEADER) Integer userID,
                           @Valid @RequestBody ItemDto itemDto) {
        return itemService.addItem(itemDto, userID);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(USER_ID_HEADER) Integer userID,
                              @PathVariable int itemId,
                              @RequestBody ItemDto itemDto) {
        return itemService.updateItem(itemDto.toBuilder().id(itemId).build(), userID);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable int itemId) {
        return itemService.getItemByID(itemId);
    }

    @GetMapping
    public List<ItemDto> getItemsForUser(@RequestHeader(USER_ID_HEADER) Integer userID) {
        return itemService.getItemsForUser(userID);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItemsByText(@RequestParam String text) {
        return itemService.searchItemsByText(text);
    }

}
