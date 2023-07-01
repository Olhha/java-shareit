package ru.practicum.shareit.request.dto;


import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotBlank;

@Value
@Builder(toBuilder = true)
public class ItemRequestDto {
    long id;
    @NotBlank
    String description;
    Long requesterId;
}
