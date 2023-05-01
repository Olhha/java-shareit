package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final BookingServiceImpl bookingService;

    @Autowired
    public BookingController(BookingServiceImpl bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingResponseDto addBooking(@RequestHeader(USER_ID_HEADER) Long userID,
                                         @Valid @RequestBody BookingRequestDto bookingRequestDto) {
        return bookingService.addBooking(bookingRequestDto.toBuilder().bookerId(userID).build());
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approveBooking(@RequestHeader(USER_ID_HEADER) Long userID,
                                             @PathVariable Long bookingId,
                                             @RequestParam String approved) {
        return bookingService.approveBooking(userID, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBookingById(@RequestHeader(USER_ID_HEADER) Long userID,
                                             @PathVariable Long bookingId) {
        return bookingService.getBookingByIdByOwnerOrBooker(userID, bookingId);
    }


    @GetMapping
    public List<BookingResponseDto> getAllBookingsForUser(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(required = false, defaultValue = "ALL") String state) {
        return bookingService.getAllBookingsForUser(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getAllBookingsForOwner(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(required = false, defaultValue = "ALL") String state) {
        return bookingService.getAllBookingsForOwner(userId, state);
    }
}
