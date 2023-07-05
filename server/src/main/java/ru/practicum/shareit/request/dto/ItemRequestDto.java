package ru.practicum.shareit.request.dto;


import lombok.Builder;
import lombok.Value;


@Value
@Builder(toBuilder = true)
public class ItemRequestDto {
    long id;
    String description;
    Long requesterId;
}
