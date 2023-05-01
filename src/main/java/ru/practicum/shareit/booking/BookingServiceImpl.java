package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository,
                              UserRepository userRepository, ItemRepository itemRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    public BookingResponseDto addBooking(BookingRequestDto bookingRequestDto) {
        checkDates(bookingRequestDto.getStart(), bookingRequestDto.getEnd());

        Booking booking = BookingMapper.toBooking(bookingRequestDto);
        Long itemId = bookingRequestDto.getItemId();
        booking.setItem(itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format(
                        "Item with id = %d doesn't exist.", itemId))));

        checkIfItemIsAvailable(booking);
        checkIfBookerIsNotOwner(bookingRequestDto, booking);


        Long bookerId = bookingRequestDto.getBookerId();
        booking.setBooker(getUserById(bookerId));

        booking.setStatus(Status.WAITING);

        Booking bookingSaved = bookingRepository.save(booking);
        return BookingMapper.toBookingDto(bookingSaved);
    }

    public BookingResponseDto approveBooking(Long userID, Long bookingId, String approved) {
        boolean approvedParsed = checkApproved(approved);

        Booking booking = getBookingById(bookingId);

        if (booking.getItem().getOwner().getId() != userID) {
            throw new NotFoundException("You are not the item's owner. " +
                    "The only owner allowed to approve the booking.");
        }
        if (booking.getStatus().equals(Status.APPROVED)) {
            throw new ValidationException("Booking already approved.");
        }

        if (approvedParsed) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }
        bookingRepository.save(booking);

        return BookingMapper.toBookingDto(booking);
    }

    private boolean checkApproved(String approved) {
        if (approved.equalsIgnoreCase("true")) {
            return true;
        } else if (approved.equalsIgnoreCase("false")) {
            return false;
        } else {
            throw new ValidationException("Approved value is not valid.");
        }
    }

    private Booking getBookingById(long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format(
                        "Booking with id = %d doesn't exist.", bookingId)));
    }

    private void checkIfBookerIsNotOwner(BookingRequestDto bookingRequestDto, Booking booking) {
        long bookerId = bookingRequestDto.getBookerId();
        long ownerId = booking.getItem().getOwner().getId();
        if (bookerId == ownerId) {
            throw new NotFoundException("Owners can't book their own items.");
        }
    }

    private void checkIfItemIsAvailable(Booking booking) {
        if (!booking.getItem().getAvailable()) {
            throw new ValidationException(String.format(
                    "Item with id = %d isn't available for booking.",
                    booking.getItem().getId()));
        }
    }

    private void checkDates(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new ValidationException("Start and End dates can't be empty");
        }
        if (start.isEqual(end)) {
            throw new ValidationException("Start and End dates can't be equal");
        }
        if (end.isBefore(start)) {
            throw new ValidationException("End should be after Start");
        }
        if (start.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Start can't be in past");
        }
    }

    public BookingResponseDto getBookingByIdByOwnerOrBooker(Long userID, Long bookingId) {
        Booking booking = getBookingById(bookingId);
        getUserById(userID);

        if ((booking.getBooker().getId() != userID)
                && (booking.getItem().getOwner().getId() != userID)) {
            throw new NotFoundException("Sorry, only item's Owner or Booker can view the booking.");
        }
        return BookingMapper.toBookingDto(booking);
    }

    public List<BookingResponseDto> getAllBookingsForUser(Long userId, String state) {
        getUserById(userId);

        StateForRequest stateForRequest = parseState(state);

        List<Booking> bookings = new ArrayList<>();

        switch (stateForRequest) {
            case ALL:
                bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId);
                break;
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        userId, LocalDateTime.now(), LocalDateTime.now());
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(
                        userId, LocalDateTime.now());
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartIsAfterOrderByStartDesc(
                        userId, LocalDateTime.now());
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                        userId, Status.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                        userId, Status.REJECTED);
                break;

        }
        return bookingsListToDtoList(bookings);
    }

    public List<BookingResponseDto> getAllBookingsForOwner(Long ownerId, String state) {
        getUserById(ownerId);

        StateForRequest stateForRequest = parseState(state);

        List<Booking> bookings = new ArrayList<>();

        switch (stateForRequest) {
            case ALL:
                bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(ownerId);
                break;
            case CURRENT:
                bookings = bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        ownerId, LocalDateTime.now(), LocalDateTime.now());
                break;
            case PAST:
                bookings = bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(
                        ownerId, LocalDateTime.now());
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemOwnerIdAndStartIsAfterOrderByStartDesc(
                        ownerId, LocalDateTime.now());
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                        ownerId, Status.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                        ownerId, Status.REJECTED);
                break;
        }
        return bookingsListToDtoList(bookings);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(
                        "User with id = %d doesn't exist.", userId)));
    }

    private StateForRequest parseState(String state) {

        for (StateForRequest s : StateForRequest.values()) {
            if (s.name().equalsIgnoreCase(state)) {
                return s;
            }
        }
        throw new ValidationException("Unknown state: " + state);
    }

    private static List<BookingResponseDto> bookingsListToDtoList(List<Booking> bookings) {
        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }
}
