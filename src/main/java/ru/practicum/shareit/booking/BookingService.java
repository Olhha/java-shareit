package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {
    BookingResponseDto addBooking(BookingRequestDto bookingRequestDto);

    BookingResponseDto approveBooking(Long userID, Long bookingId, Boolean approved);

    BookingResponseDto getBookingByIdByOwnerOrBooker(Long userID, Long bookingId);

    List<BookingResponseDto> getAllBookingsForOwner(
            Long ownerId, String state, Integer from, Integer size);

    List<BookingResponseDto> getAllBookingsForUser(Long userId, String state,
                                                   Integer from, Integer size);
}
