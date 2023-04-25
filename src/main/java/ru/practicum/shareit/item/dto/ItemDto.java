package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Value
@Builder(toBuilder = true)
public class ItemDto {
    Integer id;
    @NotBlank
    String name;
    @NotNull
    String description;
    @NotNull
    Boolean available;
    Integer requestID;
}
