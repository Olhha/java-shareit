package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
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
        if (items == null) {
            return List.of();
        }

        Map<Long, List<Booking>> bookings = Optional.ofNullable(bookingRepository
                        .findByStatusAndItemInOrderByStartDesc(Status.APPROVED, items))
                .orElseGet(Collections::emptyList)
                .stream()
                .collect(groupingBy(b -> b.getItem().getId()));

        Map<Long, List<Comment>> comments = Optional.ofNullable(commentRepository
                        .findByItemInOrderByCreatedDesc(items))
                .orElseGet(Collections::emptyList)
                .stream()
                .collect(groupingBy(c -> c.getItem().getId()));

        return
                items.stream()
                        .map(i -> getItemWithLastAndNextBookingsDtoLocal(i, bookings, comments))
                        .collect(Collectors.toList());
    }

    private ItemWithLastAndNextBookingsAndCommentsDto getItemWithLastAndNextBookingsDtoLocal(
            Item item, Map<Long, List<Booking>> bookings, Map<Long, List<Comment>> comments) {
        Long itemId = item.getId();

        Booking lastBooking = Optional.ofNullable(bookings.get(itemId))
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(b -> b.getStart().isBefore(LocalDateTime.now()))
                .max(Comparator.comparing(Booking::getStart))
                .orElse(null);

        Booking nextBooking = Optional.ofNullable(bookings.get(itemId))
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(b -> b.getStart().isAfter(LocalDateTime.now()))
                .min(Comparator.comparing(Booking::getStart))
                .orElse(null);

        return ItemMapper.toItemWithLastNextDatesAndCommentsDto(item,
                BookingMapper.toBookingLastNextDto(lastBooking),
                BookingMapper.toBookingLastNextDto(nextBooking),
                CommentMapper.toCommentDtoList(
                        comments.getOrDefault(itemId, List.of())));
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
        checkIfUserCanComment(userID, itemId);

        Comment comment = CommentMapper.toComment(commentRequestDto);
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
