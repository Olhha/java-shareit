package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UpdateForbiddenException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithLastAndNextBookingsAndCommentsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository,
                           UserRepository userRepository,
                           BookingRepository bookingRepository,
                           CommentRepository commentRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public ItemDto addItem(ItemDto itemDto, Long userID) {
        User user = getUser(userID);

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(user);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, Long userID) {
        Item item = getItem(itemDto.getId());

        String nameUpdate = itemDto.getName();
        String descriptionUpdate = itemDto.getDescription();
        Boolean availableUpdate = itemDto.getAvailable();

        validateOwner(userID, item);

        setUpdate(item, nameUpdate, descriptionUpdate, availableUpdate);

        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    private static void validateOwner(Long userID, Item item) {
        if (userID == null) {
            throw new ValidationException("Owner id is empty.");
        }

        if (item.getOwner().getId() != userID) {
            throw new UpdateForbiddenException(String.format(
                    "User id = %d isn't the owner of item id = %d.", userID, item.getId()));
        }
    }

    private static void setUpdate(Item item, String nameUpdate, String descriptionUpdate,
                                  Boolean availableUpdate) {
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
    public ItemWithLastAndNextBookingsAndCommentsDto getItemByID(long itemId, Long userId) {
        getUser(userId);
        Item item = getItem(itemId);

        if (item.getOwner().getId() == userId) {
            return getItemWithLastAndNextBookingsDto(item);
        }
        List<Comment> comments = commentRepository.findCommentsByItemId(itemId);

        return ItemMapper.toItemWithLastNextDatesAndCommentsDto(
                item, null, null,
                CommentMapper.toCommentDtoList(comments));
    }

    @Override
    public List<ItemWithLastAndNextBookingsAndCommentsDto> getItemsForUser(Long userID) {
        List<Item> items = itemRepository.findByOwnerId(userID);

        return items.stream()
                .map(this::getItemWithLastAndNextBookingsDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItemsByText(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        List<Item> itemsFound = itemRepository.searchItemsByText(text);

        return itemsListToDtoList(itemsFound);
    }

    private ItemWithLastAndNextBookingsAndCommentsDto getItemWithLastAndNextBookingsDto(
            Item item) {
        Booking lastBooking = bookingRepository
                .getFirstByItemIdAndStatusAndStartBeforeOrderByStartDesc(
                        item.getId(), Status.APPROVED, LocalDateTime.now());

        Booking nextBooking = bookingRepository
                .getFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
                        item.getId(), Status.APPROVED, LocalDateTime.now());

        List<Comment> comments = commentRepository.findCommentsByItemId(item.getId());

        return ItemMapper.toItemWithLastNextDatesAndCommentsDto(item,
                BookingMapper.toBookingLastNextDto(lastBooking),
                BookingMapper.toBookingLastNextDto(nextBooking),
                CommentMapper.toCommentDtoList(comments));
    }

    @Override
    public CommentDto addCommentToItem(Long itemId, Long userID, CommentDto commentDto) {
        checkIfUserCanComment(userID, itemId);

        Comment comment = CommentMapper.toComment(commentDto);
        comment.setItem(getItem(itemId));
        comment.setAuthor(getUser(userID));
        comment.setCreated(LocalDateTime.now());

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private void checkIfUserCanComment(Long userID, Long itemId) {
        List<Booking> bookings = bookingRepository.findByBookerIdAndItemIdAndEndBefore(
                userID, itemId, LocalDateTime.now());

        if (bookings.isEmpty()) {
            throw new ValidationException("User id = %d can't comment on the Item id = %d");
        }
    }

    private static List<ItemDto> itemsListToDtoList(List<Item> items) {
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private Item getItem(long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(
                        "There's no item with id = " + itemId));
    }

    private User getUser(Long userID) {
        return userRepository.findById(userID).orElseThrow(
                () -> new NotFoundException(String.format(
                        "User with id = %d doesn't exist", userID)));
    }
}
