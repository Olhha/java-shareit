package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@Validated
@RestController
@RequestMapping("/items")
public class ItemController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemClient itemClient;

    @Autowired
    public ItemController(ItemClient itemClient) {
        this.itemClient = itemClient;
    }

    @PostMapping
    public ResponseEntity<Object> addItem(@RequestHeader(USER_ID_HEADER) Long userID,
                                          @Valid @RequestBody ItemDto itemDto) {
        return itemClient.addItem(userID, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(USER_ID_HEADER) Long userID,
                                             @PathVariable long itemId,
                                             @RequestBody ItemDto itemDto) {
        return itemClient.updateItem(itemDto.toBuilder().id(itemId).build(), userID);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(
            @RequestHeader(USER_ID_HEADER) Long userID,
            @PathVariable long itemId) {
        return itemClient.getItemByID(itemId, userID);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsForUser(
            @RequestHeader(USER_ID_HEADER) Long userID,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        return itemClient.getItemsForUser(userID, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItemsByText(
            @RequestHeader(USER_ID_HEADER) Long userID,
            @RequestParam String text,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        return itemClient.searchItemsByText(text, from, size, userID);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addCommentToItem(
            @RequestHeader(USER_ID_HEADER) Long userID,
            @PathVariable Long itemId,
            @Valid @RequestBody CommentRequestDto commentRequestDto) {
        return itemClient.addCommentToItem(itemId, userID, commentRequestDto);
    }

}