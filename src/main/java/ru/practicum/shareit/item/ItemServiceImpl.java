package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UpdateForbiddenException;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithLastAndNextBookingsAndCommentsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
@Transactional(readOnly = true)
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
    @Transactional
    public ItemDto addItem(ItemDto itemDto, Long userID) {
        User user = getUser(userID);

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(user);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto updateItem(ItemDto itemDto, Long userID) {
        Item item = getItem(itemDto.getId());

        String nameUpdate = itemDto.getName();
        String descriptionUpdate = itemDto.getDescription();
        Boolean availableUpdate = itemDto.getAvailable();

        validateOwner(userID, item);

        setUpdate(item, nameUpdate, descriptionUpdate, availableUpdate);

        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemWithLastAndNextBookingsAndCommentsDto getItemByID(long itemId, Long userId) {
        getUser(userId);
        Item item = getItem(itemId);

        if (item.getOwner().getId() == userId) {
            return getItemWithLastAndNextBookingsDto(item, LocalDateTime.now());
        }
        List<Comment> comments = commentRepository.findCommentsByItemId(itemId);

        return ItemMapper.toItemWithLastNextDatesAndCommentsDto(
                item, null, null,
                CommentMapper.toCommentDtoList(comments));
    }

    @Override
    public List<ItemWithLastAndNextBookingsAndCommentsDto> getItemsForUser(Long userID) {
        List<Item> items = itemRepository.findByOwnerId(userID);
        if (items == null) {
            return List.of();
        }

        Map<Long, List<Booking>> bookings = bookingRepository
                .findByStatusAndItemInOrderByStartDesc(Status.APPROVED, items)
                .stream()
                .collect(groupingBy(b -> b.getItem().getId()));

        Map<Long, List<Comment>> comments = commentRepository
                .findByItemIn(items, Sort.by(Sort.Direction.DESC, "created"))
                .stream()
                .collect(groupingBy(c -> c.getItem().getId()));

        LocalDateTime timeStamp = LocalDateTime.now();
        return
                items.stream()
                        .map(i -> getItemWithLastAndNextBookingsDtoLocal(
                                i, bookings, comments, timeStamp))
                        .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItemsByText(String text) {
        if (text.isBlank()) {
            return List.of();
        }
        List<Item> itemsFound = itemRepository.searchItemsByText(text);

        return itemsListToDtoList(itemsFound);
    }

    @Override
    @Transactional
    public CommentResponseDto addCommentToItem(Long itemId, Long userID,
                                               CommentRequestDto commentRequestDto) {
        LocalDateTime timeStamp = LocalDateTime.now();

        checkIfUserCanComment(userID, itemId, timeStamp);

        Comment comment = CommentMapper.toComment(commentRequestDto);
        comment.setItem(getItem(itemId));
        comment.setAuthor(getUser(userID));
        comment.setCreated(timeStamp);

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private void checkIfUserCanComment(Long userID, Long itemId, LocalDateTime timeStamp) {
        List<Booking> bookings = bookingRepository.findByBookerIdAndItemIdAndEndBefore(
                userID, itemId, timeStamp);

        if (bookings.isEmpty()) {
            throw new ValidationException("User id = %d can't comment on the Item id = %d");
        }
    }

    private ItemWithLastAndNextBookingsAndCommentsDto getItemWithLastAndNextBookingsDto(
            Item item, LocalDateTime timeStamp) {
        Booking lastBooking = bookingRepository
                .getFirstByItemIdAndStatusAndStartLessThanEqualOrderByStartDesc(
                        item.getId(), Status.APPROVED, timeStamp);

        Booking nextBooking = bookingRepository
                .getFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
                        item.getId(), Status.APPROVED, timeStamp);

        List<Comment> comments = commentRepository.findCommentsByItemId(item.getId());

        return ItemMapper.toItemWithLastNextDatesAndCommentsDto(item,
                BookingMapper.toBookingLastNextDto(lastBooking),
                BookingMapper.toBookingLastNextDto(nextBooking),
                CommentMapper.toCommentDtoList(comments));
    }

    private ItemWithLastAndNextBookingsAndCommentsDto getItemWithLastAndNextBookingsDtoLocal(
            Item item, Map<Long, List<Booking>> bookings, Map<Long,
            List<Comment>> comments, LocalDateTime timeStamp) {
        Long itemId = item.getId();

        Booking lastBooking = bookings.getOrDefault(itemId, List.of())
                .stream()
                .filter(b -> !b.getStart().isAfter(LocalDateTime.now()))
                .findFirst()
                .orElse(null);

        Booking nextBooking = bookings.getOrDefault(itemId, List.of())
                .stream()
                .filter(b -> b.getStart().isAfter(timeStamp))
                .reduce((b1, b2) -> b2)
                .orElse(null);

        return ItemMapper.toItemWithLastNextDatesAndCommentsDto(item,
                BookingMapper.toBookingLastNextDto(lastBooking),
                BookingMapper.toBookingLastNextDto(nextBooking),
                CommentMapper.toCommentDtoList(
                        comments.getOrDefault(itemId, List.of())));
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
        if ((nameUpdate != null) && (!nameUpdate.isBlank())) {
            item.setName(nameUpdate);
        }

        if ((descriptionUpdate != null) && (!descriptionUpdate.isBlank())) {
            item.setDescription(descriptionUpdate);
        }

        if (availableUpdate != null) {
            item.setAvailable(availableUpdate);
        }
    }
}
