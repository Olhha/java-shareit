package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.*;
import ru.practicum.shareit.booking.model.Booking;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookingMapper {
    public static BookingResponseDto toBookingDto(Booking booking) {
        return BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(new BookingItemDto(booking.getItem().getId(), booking.getItem().getName()))
                .booker(new BookerDto(booking.getBooker().getId()))
                .status(booking.getStatus().toString())
                .build();
    }

    public static BookingLastNextDto toBookingLastNextDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        return BookingLastNextDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemId(booking.getItem().getId())
                .bookerId(booking.getBooker().getId())
                .status(booking.getStatus().toString())
                .build();
    }

    public static Booking toBooking(BookingRequestDto bookingRequestDto) {
        return Booking.builder()
                .start(bookingRequestDto.getStart())
                .end(bookingRequestDto.getEnd())
                .build();
    }
}
