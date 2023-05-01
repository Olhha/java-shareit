package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Value
@Builder(toBuilder = true)
public class BookingRequestDto {
    long id;
    @NotNull
    LocalDateTime start;
    @NotNull
    LocalDateTime end;
    @NotNull
    Long itemId;

    Long bookerId;
    String status;
}
