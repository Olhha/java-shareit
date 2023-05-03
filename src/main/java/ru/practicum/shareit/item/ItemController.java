package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithLastAndNextBookingsAndCommentsDto;

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
    public ItemDto addItem(@RequestHeader(USER_ID_HEADER) Long userID,
                           @Valid @RequestBody ItemDto itemDto) {
        return itemService.addItem(itemDto, userID);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(USER_ID_HEADER) Long userID,
                              @PathVariable long itemId,
                              @RequestBody ItemDto itemDto) {
        return itemService.updateItem(itemDto.toBuilder().id(itemId).build(), userID);
    }

    @GetMapping("/{itemId}")
    public ItemWithLastAndNextBookingsAndCommentsDto getItem(
            @RequestHeader(USER_ID_HEADER) Long userID,
            @PathVariable long itemId) {
        return itemService.getItemByID(itemId, userID);
    }

    @GetMapping
    public List<ItemWithLastAndNextBookingsAndCommentsDto> getItemsForUser(
            @RequestHeader(USER_ID_HEADER) Long userID) {
        return itemService.getItemsForUser(userID);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItemsByText(@RequestParam String text) {
        return itemService.searchItemsByText(text);
    }

    @PostMapping("{itemId}/comment")
    public CommentResponseDto addCommentToItem(@RequestHeader(USER_ID_HEADER) Long userID,
                                               @PathVariable Long itemId,
                                               @Valid @RequestBody CommentRequestDto commentRequestDto) {
        return itemService.addCommentToItem(itemId, userID, commentRequestDto);
    }

}
