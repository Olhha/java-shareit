package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
public class BookingRequestDto {
    LocalDateTime start;
    LocalDateTime end;
    Long itemId;
    @JsonIgnore
    Long bookerId;
}
